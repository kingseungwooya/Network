package chat_file;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
/**
 * 송신:
 *  상위 계층으로부터 데이터를 전달받으면 그 데이터를 프레임의 데이터에 저장수신될 Ethernet 주소와 자신의 Ethernet 주소를 헤더에 저장
 상위 계층의 종류에 따라서 헤더에 상위 프로토콜 형태 저장 후 물리적 계층으로 Ethernet frame 전달(enet_type)
 0x2080: ChattingApp Layer
 0x2090: FileApp Layer
   수신:하위 계층(physical layer)로부터 프레임을 받으면 상위로 보내야 하는지, 혹은 폐기해야
하는지 결정
 상위 계층으로 보내는 기준 (아래 둘을 제외하고는 전부 폐기)목적지 Ethernet 주소가 브로드캐스트 주소(ff-ff-ff-ff-ff-ff)일 경우
 목적지 Ethernet 주소가 자신의 Ethernet 주소일 경우
 Ethernet 프레임 헤더 중에 16 비트 프로토콜 타입 필드를 보고 판단하여 상위 계층으로
전달(enet_type)
 0x2080: ChattingApp Layer
 0x2090: FileApp Layer
   	프레임 최대 MTU 1500bytes 
 * @author ksw_0
 *
 */
public class EthernetLayer implements BaseLayer {
	public int nUpperLayerCount = 0;
	public String pLayerName = null;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();

	private class _ETHERNET_ADDR {
		private byte[] addr = new byte[6];

		public _ETHERNET_ADDR() {
			this.addr[0] = (byte) 0x00;
			this.addr[1] = (byte) 0x00;
			this.addr[2] = (byte) 0x00;
			this.addr[3] = (byte) 0x00;
			this.addr[4] = (byte) 0x00;
			this.addr[5] = (byte) 0x00;
		}
	}

	private class _ETHERNET_HEADER {
		_ETHERNET_ADDR enet_dstaddr; //6
		_ETHERNET_ADDR enet_srcaddr; //6
		byte[] enet_type; //2
		byte[] enet_data;//46-1500 

		public _ETHERNET_HEADER() {
			this.enet_dstaddr = new _ETHERNET_ADDR();
			this.enet_srcaddr = new _ETHERNET_ADDR();
			this.enet_type = new byte[2];
			this.enet_data = null;
		}
	}

	_ETHERNET_HEADER m_sHeader = new _ETHERNET_HEADER();

	public EthernetLayer(String pName) {
		// super(pName);
		// TODO Auto-generated constructor stub
		pLayerName = pName;
		ResetHeader();
	}

	public void ResetHeader() {
		for (int i = 0; i < 6; i++) {
			m_sHeader.enet_dstaddr.addr[i] = (byte) 0x00;
			m_sHeader.enet_srcaddr.addr[i] = (byte) 0x00;
		}
		m_sHeader.enet_type[0] = (byte) 0x00;
		m_sHeader.enet_type[1] = (byte) 0x00;
		m_sHeader.enet_data = null;
	}
	  private int byte2ToInt(byte value1, byte value2) {
	        return (int)((value1 << 8) | (value2));
	    }
	    /**
	     * 채팅 Send 그리고 Port설정 
	     */
		public boolean Send(byte[] input, int length) {
			m_sHeader.enet_type = intToByte2(0x2080); 
			m_sHeader.enet_data = input;
			byte[] bytes = encapsulatioin(m_sHeader, input, length);
			this.GetUnderLayer().Send(bytes, length + 14);

			return true;
		}
		
		public boolean fileSend(byte[] input, int length) {
			m_sHeader.enet_type = intToByte2(0x2090);
			byte[] bytes = encapsulatioin(m_sHeader, input, length);
			this.GetUnderLayer().Send(bytes, length + 14);
			return true;
		}
		
		public boolean ARPSend(byte[] input, int length) {
			m_sHeader.enet_type = intToByte2(0x0806);
			m_sHeader.enet_data = input;
			byte[] bytes = encapsulatioin(m_sHeader, input, length);
			this.GetUnderLayer().Send(bytes, length+14);
			return true;
		}
		//이더넷 헤더를 분리함 
		public byte[] decapsulation(byte[] input, int length) {
			byte[] data = new byte[length - 14];
			for (int i = 0; i < length - 14; i++)
				data[i] = input[14 + i];
			return data;
		}
		//출발지 맥주소와 같은지 확인 
		public boolean IsItMyPacket(byte[] input) {
			for (int i = 0; i < 6; i++) {
				if (m_sHeader.enet_srcaddr.addr[i] == input[6 + i])
					continue;
				else
					return false;
			}
			return true;
		}
		
		/**
		 * src 맥주소와 Receive시 받은 목적지 주소와 같은지 확인 
		 */
		public boolean IsItMine(byte[] input) {
			for (int i = 0; i < 6; i++) {
				if (m_sHeader.enet_srcaddr.addr[i] == input[i])
					continue;
				else {
					return false;
				}
			}
			return true;
		}
		
		
		//목적지 Mac address주소가 BroadCast주소로 되어있는가> ?
		public boolean IsItBroadcast(byte[] input) {
			for (int i = 0; i < 6; i++) {
				if (input[i] == 0xff) {
					continue;
				} else
					return false;
			}
			return true;
		}

		public boolean Receive(byte[] input) {
			byte[] data;
			System.out.println("ethernet receive");
			int temp_type = byte2ToInt(input[12], input[13]);
			System.out.println(temp_type);
			if(temp_type == Integer.decode("0x2080")) { //data
				System.out.println("2080");
				if(IsItMine(input) || (IsItBroadcast(input)) || !IsItMyPacket(input)) {
					data = decapsulation(input, input.length);
					this.GetUpperLayer(0).Receive(data);
					return true;
				}
			}
			else if(temp_type == Integer.decode("0x2090")) { //file
				System.out.println("2090");
				if(IsItMine(input) || (IsItBroadcast(input)) || !IsItMyPacket(input)) {
					data = decapsulation(input, input.length);
					this.GetUpperLayer(1).Receive(data);
					return true;
				}
			}else if(temp_type == Integer.decode("0x0806")) {
				System.out.println("0806");	
				System.out.println("ethernet arp receive");
				temp_type = byte2ToInt(input[12], input[13]);
				if(temp_type == Integer.decode("0x0806")) {
					
					if(IsItMine(input) || !IsItMyPacket(input) || (IsItBroadcast(input))) {	// 
						data = decapsulation(input, input.length);
						((ARPLayer) this.GetUpperLayer(0)).receive(data);
						return true;
					}
				}
				return false;
			}
			return false; 
		
		}
		
		
	//위 게층에서 받아온 Data에 이더넷 헤더를 붙여 encapsulation함 
	public byte[] encapsulatioin(_ETHERNET_HEADER Header, byte[] input, int length) {//data占쎈� 占쎈엘占쎈�� �븐��肉т��⑤┛
		byte[] buf = new byte[length + 14];
		for(int i = 0; i < 6; i++) {
			buf[i] = Header.enet_dstaddr.addr[i];
			buf[i+6] = Header.enet_srcaddr.addr[i];
		}			
		buf[12] = Header.enet_type[0];
		buf[13] = Header.enet_type[1];
		for (int i = 0; i < length; i++)
			buf[14 + i] = input[i];

		return buf;
	}
	
	private byte[] intToByte2(int value) {
        byte[] temp = new byte[2];
        temp[0] |= (byte) ((value & 0xFF00) >> 8);
        temp[1] |= (byte) (value & 0xFF);

        return temp;
    }

	public _ETHERNET_ADDR GetEnetDstAddress() {
		return m_sHeader.enet_dstaddr;
	}

	public _ETHERNET_ADDR GetEnetSrcAddress() {
		return m_sHeader.enet_srcaddr;
	}

	public void SetEnetType(byte[] input) {
		for (int i = 0; i < 2; i++) {
			m_sHeader.enet_type[i] = input[i];
		}
	}

	public void SetEnetDstAddress(byte[] input) {
		for (int i = 0; i < 6; i++) {
			m_sHeader.enet_dstaddr.addr[i] = input[i];
		}
	}

	public void SetEnetSrcAddress(byte[] input) {
		for (int i = 0; i < 6; i++) {
			m_sHeader.enet_srcaddr.addr[i] = input[i];
		}
	}

	
	@Override
	public void SetUnderLayer(BaseLayer pUnderLayer) {
		// TODO Auto-generated method stub
		if (pUnderLayer == null)
			return;
		this.p_UnderLayer = pUnderLayer;
	}

	@Override
	public void SetUpperLayer(BaseLayer pUpperLayer) {
		// TODO Auto-generated method stub
		if (pUpperLayer == null)
			return;
		this.p_aUpperLayer.add(nUpperLayerCount++, pUpperLayer);
	}

	@Override
	public String GetLayerName() {
		// TODO Auto-generated method stub
		return pLayerName;
	}

	@Override
	public BaseLayer GetUnderLayer() {
		// TODO Auto-generated method stub
		if (p_UnderLayer == null)
			return null;
		return p_UnderLayer;
	}

	@Override
	public BaseLayer GetUpperLayer(int nindex) {
		// TODO Auto-generated method stub
		if (nindex < 0 || nindex > nUpperLayerCount || nUpperLayerCount < 0)
			return null;
		return p_aUpperLayer.get(nindex);
	}

	@Override
	public void SetUpperUnderLayer(BaseLayer pUULayer) {
		this.SetUpperLayer(pUULayer);
		pUULayer.SetUnderLayer(this);

	}
}
