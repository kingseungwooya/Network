package chat_file;

import java.nio.ByteBuffer;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jnetpcap.ByteBufferHandler;
import org.jnetpcap.Pcap;
import org.jnetpcap.PcapIf;
import org.jnetpcap.packet.PcapPacket;
import org.jnetpcap.packet.PcapPacketHandler;

/* JnetPcap을 이용하여 기본적인 packet 송수신 operation 구현한 class
 Adapter와 상위 Layer간의 데이터 송수신의 중간자 역할을 담당
 Mac 정보를 얻어온다
 * 
 */
public class NILayer implements BaseLayer {

	public int nUpperLayerCount = 0;
	public String pLayerName = null;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();

	int m_iNumAdapter; //네트워크 어뎁터 인덱스
	public Pcap m_AdapterObject; //네트워크 어뎁터 객체
	public PcapIf device; //네트워크 인터페이스 객체
	public List<PcapIf> m_pAdapterList; //네트워크 인터페이스 목록
	StringBuilder errbuf = new StringBuilder(); //에러버퍼 

	public NILayer(String pName) {
		// super(pName);
		pLayerName = pName;

		m_pAdapterList = new ArrayList<PcapIf>(); //동적할당 
		m_iNumAdapter = 0; //초기화
		SetAdapterList();
	}

	public void PacketStartDriver() {
		//pcap 동작에 필요한 변수들
		int snaplen = 64 * 1024; // Capture all packets, no trucation 패킷 캡쳐 길이
		int flags = Pcap.MODE_PROMISCUOUS; // capture all packets   모든 패킷 캡쳐 플래그
		int timeout = 10 * 1000; // 10 seconds in millis 패킷 캡처 시간 : 설정시간동안 패킷이 수신되지 않은 경우 에러버퍼 입력
		//pcap 작동시작  
		m_AdapterObject = Pcap.openLive(m_pAdapterList.get(m_iNumAdapter).getName(), snaplen, flags, timeout, errbuf);
	}

	public PcapIf GetAdapterObject(int iIndex) {
		return m_pAdapterList.get(iIndex);
	}

	public void SetAdapterNumber(int iNum) {
		m_iNumAdapter = iNum; //변수 초기화
		PacketStartDriver(); //패킷드라이버 시작함수(네트워크 어뎁터 객체 open)
		Receive(); // 패킷 수신 함수
	}

	public void SetAdapterList() {
		int r = Pcap.findAllDevs(m_pAdapterList, errbuf); //현재 컴퓨터에 존재하는 모든 네트워크 어뎁터 목록 가져오기 
		if (r == Pcap.NOT_OK || m_pAdapterList.isEmpty()) {
			System.err.printf("Can't read list of devices, error is %s", errbuf.toString());
			return; //네으퉈크 어뎁터 하나도 없을시 에러처리 
		}
	}
	//패킷 전송함수
	public boolean Send(byte[] input, int length) {
		
		ByteBuffer buf = ByteBuffer.wrap(input); //상위레이어로부터 받은 데이터 바이트 퍼버처에 담음
		if (m_AdapterObject.sendPacket(buf) != Pcap.OK) { //네트워크 어뎁터로 데이터 전송
			System.err.println(m_AdapterObject.getErr());
			return false;
		}
		return true;
	}

	public boolean Receive() {
		Receive_Thread thread = new Receive_Thread(m_AdapterObject, this.GetUpperLayer(0));
		Thread obj = new Thread(thread); //쓰레드 생성
		obj.start(); //쓰레드 시작

		return false;
	}

	@Override
	public void SetUnderLayer(BaseLayer pUnderLayer) {
		// TODO Auto-generated method stub
		if (pUnderLayer == null)
			return;
		p_UnderLayer = pUnderLayer;
	}

	@Override
	public void SetUpperLayer(BaseLayer pUpperLayer) {
		// TODO Auto-generated method stub
		if (pUpperLayer == null)
			return;
		this.p_aUpperLayer.add(nUpperLayerCount++, pUpperLayer);
		// nUpperLayerCount++;
	}

	@Override
	public String GetLayerName() {
		// TODO Auto-generated method stub
		return pLayerName;
	}

	@Override
	public BaseLayer GetUnderLayer() {
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
//패킷 수신 시 패킷 처리를 위한 runnable 클래스 생성 
class Receive_Thread implements Runnable {
	byte[] data;
	Pcap AdapterObject;
	BaseLayer UpperLayer;

	public Receive_Thread(Pcap m_AdapterObject, BaseLayer m_UpperLayer) {
		// TODO Auto-generated constructor stub
		AdapterObject = m_AdapterObject;
		UpperLayer = m_UpperLayer;
	}

	@Override
	public void run() {
		while (true) {
			//패킷 수신을 위한 핸들러 라이브러리 함수
			PcapPacketHandler<String> jpacketHandler = new PcapPacketHandler<String>() {
				public void nextPacket(PcapPacket packet, String user) {
					data = packet.getByteArray(0, packet.size()); //수신도니 패킷의 데이터(바이트배열) 와 패킷 크기를 알아냄
					UpperLayer.Receive(data); //상위레이러로 데이터 전달 !!!!!!!
				}
			};

			AdapterObject.loop(100000, jpacketHandler, ""); //핸들러 무한루프 
		}
	}
}
