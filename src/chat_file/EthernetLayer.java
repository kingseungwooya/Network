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
		_ETHERNET_ADDR enet_dstaddr;
		_ETHERNET_ADDR enet_srcaddr;
		byte[] enet_type;
		byte[] enet_data;

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
		
	}


	public boolean Send(byte[] input, int length) {
	

		return false;
	}

	

	public boolean Receive(byte[] input) {
		
		return true;
	}

	@Override
	public void SetUnderLayer(BaseLayer pUnderLayer) {
	
	}

	@Override
	public void SetUpperLayer(BaseLayer pUpperLayer) {
		
	}

	@Override
	public String GetLayerName() {
		// TODO Auto-generated method stub
		return pLayerName;
	}

	@Override
	public BaseLayer GetUnderLayer() {
		return  null;
	}

	@Override
	public BaseLayer GetUpperLayer(int nindex) {
		return null;
	}

	@Override
	public void SetUpperUnderLayer(BaseLayer pUULayer) {
		

	}
}
