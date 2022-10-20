package chat_file;

import java.util.ArrayList;

/**
 * ip주소
 * host 168.188.129.1
 * vm1 168.188.129.2
 * vm2 168.188.129.3
 * @author ksw_0
 *
 */
public class ARPLayer implements BaseLayer  {

	Arp_Message message = new Arp_Message();
	public int nUpperLayerCount = 0;
	public String pLayerName = null;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();
	public ArrayList<ArrayList<byte[]>> cacheTable = new ArrayList<ArrayList<byte[]>>();
	public ArrayList<ArrayList<byte[]>> proxyCacheTable = new ArrayList<ArrayList<byte[]>>();
	private static LayerManager m_LayerMgr = new LayerManager();
	public int state = 0;
	public ARPLayer(String pName){
		pLayerName= pName;
		//ArpLayer이 호출되는 순간 message를 빈 값으로 초기화 
		setMessage();
	}
	/**
	 * Ip, Mac address form
	 * @author ksw_0
	 *
	 */
	class Ip_Address{
		private byte[] address= new byte[4];
		public Ip_Address(){
			this.address[0]=(byte) 0x00;
			this.address[1]=(byte) 0x00;
			this.address[2]=(byte) 0x00;
			this.address[3]=(byte) 0x00;
		}
	}
	private class Mac_Address{
		private byte[] address= new byte[6];
		public Mac_Address(){
			this.address[0]=(byte) 0x00;
			this.address[1]=(byte) 0x00;
			this.address[2]=(byte) 0x00;
			this.address[3]=(byte) 0x00;
			this.address[4]=(byte) 0x00;
			this.address[5]=(byte) 0x00;
			
		}
	}
	/**
	 * Arp Message class
	 * @author ksw_0
	 *
	 */
	private class Arp_Message{
		byte[] hardware_type; //고정 이더넷 프로토콜0001
		byte[] protocol_type; //고정 (IPv4 0800)
		byte hard_size; //고정 	hard size(addresslength=06) 
		byte protocol_size; //고정 	protocol size(addresslength=04)
	    byte[] opcode;
	    Mac_Address sender_Mac;
	    Ip_Address sender_Ip;
	    Mac_Address target_Mac;
	    Ip_Address target_Ip;
	    
	    Arp_Message(){ //총  크기 28byte 
	    	this.hardware_type=new byte[2];
	    	this.protocol_type=new byte[2];
	    	this.hard_size = 0x06;
	    	this.protocol_size = 0x04;
	    	this.opcode=new byte[2]; //000000001
	    	this.sender_Mac= new Mac_Address();
	    	this.sender_Ip = new Ip_Address();
	    	this.target_Mac= new Mac_Address();
	    	this.target_Ip = new Ip_Address();
	    
	    }
	}
	/**
	 * message 초기화 
	 */
	public void setMessage(){
		for (int i = 0; i < 6; i++) {
			message.sender_Mac.address[i] = (byte) 0x00;
			message.target_Mac.address[i] = (byte) 0x00;
		}
		for (int j = 0; j < 4; j++) {
			message.sender_Ip.address[j] = (byte) 0x00;
			message.target_Ip.address[j] = (byte) 0x00;
		}
		for (int k = 0; k < 2; k++) {
			message.opcode[k] = (byte) 0x00;
			message.hardware_type[k] = (byte) 0x00;
			message.protocol_type[k] = (byte) 0x00;
		}
		message.hard_size = 6;
		message.protocol_size = 4;
	}
	/**\
	 *  ip주소로만 찾기 
	 * @param ip_src 출발지 ip 주소
	 * @param ip_dst 도착지 ip 주소
	 */
	

	public void send(byte[] ip_src, byte[] ip_dst){ 
		Arp_Message data= decapsulation(ip_src, ip_dst);
		//frame.prottype = intToByte2(0x0800);
		//this.SetIpSrcAddress(ip_src);
		//this.SetIpDstAddress(ip_dst);
		byte[] ethernet_date = encapsulation(message, 28);
		((EthernetLayer) this.GetUnderLayer()).ARPSend(ethernet_date, 28);
		//return false;
		
	}
	//Dlg에서 받은 ip src 와 ip dst
	private Arp_Message decapsulation(byte[] ip_src,byte[] ip_dst){
		message.protocol_type = intToByte2(0x0800);
		return null;
	}
	//이더넷 레이어로부터 데이터를 받음 
	//여기서 tcp ip로 receive하지 않음 
	public boolean receive(byte[] input){
		int ARP_Request = byte2ToInt(input[6], input[7]); // ARP Opcode 

		//상황 가정
		//A에서 B로 통신을 시도하였고 이제 B에서 request 를 보낼차례 
		if (ARP_Request == 1) { // ARP Request 0001
			System.out.println("arp receive request");
			// 1.  dst가 broadcast인 경우. 즉 처음 주소 물어볼 때.
			// 각 host는 자신이 목적지인지 확인하기 전에 table에 지금 ARP 요청 보낸 host(sender)의 IP와 MAC 저장
			// 각 host는 자신이 목적지가 맞는지 확인함. -> ARP message에 있는 target IP 보고
			// 목적지 아니면 drop. 맞으면 ARP message에 있는 target mac에 자신의 MAC 주소 넣음
			// ARP 응답 메시지를 seder에데 보내기 위해 ARP message에 있는 sender's와 target's swap
			// opcode 2로 바꿈. -> 왜냐면 ARP reply위해서.
			addCacheTable(input);
			
			//A에서의 TargetIpaddress를 저장함
			//밑에 조건문에서 A에서의 TargetIpaddress와 B의 ipaddress가 같은지 확인하기 위함 
			byte[] targe_ip_address = new byte[4];
			for(int i = 0; i < 4; i++) {	
				targe_ip_address[i] = input[24+i];
			}
			
			//Swaping 24~28은 원래 target Ip address
			//A에서 B로 왔고 이젠 B에서 A로 가야하니 원래의 ArpMessage의 targetipaddress(24~28)을 출발지로 바꿔줌 
			byte[] send_ip_b = new byte[4];
			System.arraycopy(input, 24, send_ip_b, 0, 4);
			//A에서 B로 왔고 이젠 B에서 A로 가야하니 원래의 ArpMessage의 srcipaddress(14~18)을 목적지로 바꿔줌 

			byte[] target_ip_b = new byte[4];
			System.arraycopy(input, 14, target_ip_b, 0, 4);
			
			//시작 A에서 B로 도착하고 B에서 A로 출발하기 위한 여정 시작 
			if(IsItMine(targe_ip_address)) {//A에서의 Targetip가 B의 ip와 같다면?	
				
				//Arp message 재 설정 
				for(int i = 0; i < 4; i++) {	// target ip 주소 바꾸기
					message.target_Ip.address[i] = input[14+i];
				}
				
				for(int i = 0; i < 6; i++) {
					message.target_Mac.address[i] = input[8+i];
				}
				
				message.opcode = intToByte2(2);
		
				send(send_ip_b, target_ip_b); 
				message.opcode = intToByte2(1);// 다시 디폴트값 0001로 바꾸기 
				
			}else { // A에서 온 Targetip가 B의 ip가 아닐 때!! proxy에 추가 
				
				// 자신의 proxy table 확인
				boolean check = ProxyCheck(targe_ip_address);
				
				// 만약 proxy table에 target's mac 주소 있으면 target's mac 주소 채움
				// proxy table에 있으면 Dlg로 table 보내주기.
				// sender's와 target's 위치 swap.
				// opcode 2로 변경
				if(check==true) { //ip가 같지않더라도 proxy Table에 있다면? 
					message.opcode = intToByte2(2); //0002 requset
			
					send(send_ip_b, target_ip_b); //다시 B에서 A로 떠나는 여정 시작
					message.opcode = intToByte2(1); //Send 했으니 다시 디폴트 0001로 초기화 
					
					///gui에 데이터 추가 
					((ARPDlg) ARPDlg.m_LayerMgr.GetLayer("GUI")).setArpCache(cacheTable);
					}
				}

			return true;
		} else if (ARP_Request == 2) { // ARP Reply
			System.out.println("arp receive reply");

			// sender의 ARP Layer가 받음.
			// ARP messgae target's mac보고 sender는 table 채움.
			// ip, mac변수에 setting -> Dlg에서 get해서 화면에 출력
			addCacheTable(input);
			
			return true;
		}
		return false;
	}
	
	//ARP message 28byte 이므로 각 위치별 바이트배열 위치당 값들 할당
	//Arp 객체를 byte배열에 값을 다 할당 해줌      
	private byte[] encapsulation(Arp_Message msg, int length) {
		byte[] data = new byte[28];
		//opcode 정수 변환시 2이면 request 1이면 send, default는 send로 함 
		if (byte2ToInt(msg.opcode[0], msg.opcode[1]) == 2) {
			Mac_Address temp = msg.sender_Mac;
			msg.target_Mac = temp;
			msg.sender_Mac = temp;
		}

		for (int i = 0; i < 2; i++) {
			data[i] = msg.hardware_type[i];
			data[i + 2] = msg.protocol_type[i];
			data[i + 6] = msg.opcode[i];
		}

		data[4] = msg.hard_size; // 고정 
		data[5] = msg.protocol_size; //고정 

		for (int i = 0; i < 6; i++) {
			data[i + 8] = msg.sender_Mac.address[i];
			data[i + 18] = msg.target_Mac.address[i];
		}
		for (int i = 0; i < 4; i++) {
			data[i + 14] = msg.sender_Ip.address[i];
			data[i + 24] = msg.target_Ip.address[i];
		}
		return data;
	}
	public boolean addCacheTable(byte[] input){//cache table setting
	      ArrayList<byte[]> cache = new ArrayList<byte[]>();
	      //proxycacheTable dlg에서 proxy가져오기 
	      //1. input으로 들어온 src_arp의 ip와 mac주소 가져와서 cache table에 존재하는지 확인 -> 없으면 넣기
	      //2. 이미 존재하는 ip라면 table의 mac주소와 target_arp를 확인해서 틀리면 바꿔버려 -> GArp
	      
	      byte[] src_ip_address = new byte[4];
	      for(int i=0; i<4; i++) {   
	    	  src_ip_address[i] = input[14+i]; //input의 ip주소 buffer 임시저장
	      }
	      
	      byte[] src_mac_address = new byte[6];
	      for(int i=0; i<6; i++) {   
	    	  src_mac_address[i] = input[i+8]; //input의 mac주소 buffer 임시저장
	      }
	      
	      boolean hasIP = false;
	      for(int i=0; i<cacheTable.size(); i++) {
	         if(java.util.Arrays.equals(src_ip_address, cacheTable.get(i).get(0))) { //cacheTable에 ip주소가 존재
	            hasIP = true;
	            if(!java.util.Arrays.equals(src_mac_address, cacheTable.get(i).get(1))) { //cacheTable에 저장된 mac주소가 sender_mac과 다름
	               //다를 경우 캐시테이블 수정 
	               cacheTable.get(i).set(1, src_mac_address);
	            }
	         }
	      }
	      
	      if(hasIP == false) {//cacheTable에 ip주소가 존재하지 않은 경우
	         cache.add(src_ip_address);   // cache[0]에 ip 주소 넣기
	         cache.add(src_mac_address);   // cache[1]에 mac 주소 넣기
	         cache.add(intToByte2(1));  // cache[2]에  Complete넣기.1이면 complete.
	         cacheTable.add(cache);
	      }
	      //gui에 캐시테이블 올려주기 
	      ((ARPDlg)ARPDlg.m_LayerMgr.GetLayer("GUI")).setArpCache(cacheTable);
	      return true;
	   }
	
	// proxy Table에 채우는 함수
		public boolean addProxyTable(byte[] interNum, byte[] proxy_ip, byte[] proxy_mac) {
			ArrayList<byte[]> proxy = new ArrayList<byte[]>();

			proxy.add(interNum); // proxy[0]에는 interface number 넣기
			proxy.add(proxy_ip); // proxy[1]에는 ip 주소 넣기
			proxy.add(proxy_mac); // proxy[2]에는 mac 주소 넣기

			proxyCacheTable.add(proxy);

			return true;
		}
		// proxy table의 ip와 dst의 ip와 같은지 확인
		public boolean ProxyCheck(byte[] dst_ip) {
			for (int i = 0; i < proxyCacheTable.size(); i++) {
				boolean flag = true;
				ArrayList<byte[]> proxy = proxyCacheTable.get(i);
				for (int j = 0; j < 4; j++) {
					if (proxy.get(1)[j] == dst_ip[j]) {
						continue;
					} else {
						flag = false;
					}
				}
				if (flag == true) {
					return true; //proxytable에 존재함 
				}
			}
			return false;
		}

		public boolean IsItMine(byte[] input) {
			for (int i = 0; i < 4; i++) {
				if (message.sender_Ip.address[i] == input[i])
					continue;
				else {
					return false;
				}
			}
			return true;
		}
	
	private byte[] intToByte2(int value) {
		byte[] temp = new byte[2];
		temp[0] |= (byte) ((value & 0xFF00) >> 8);
		temp[1] |= (byte) (value & 0xFF);

		return temp;
	}

	private int byte2ToInt(byte value1, byte value2) {
		return (int) ((value1 << 8) | (value2));
	}

	
	@Override
	public String GetLayerName() {
		return pLayerName;
	}
	@Override
	public BaseLayer GetUnderLayer() {
		return p_UnderLayer;
	}
	@Override
	public BaseLayer GetUpperLayer(int nindex) {
		return p_aUpperLayer.get(nindex); //34567 layer중 index를 통해 get
	}
	@Override
	public void SetUnderLayer(BaseLayer pUnderLayer) {
		this.p_UnderLayer =pUnderLayer; // arp의 밑  layer는 이더넷 
	}
	@Override
	public void SetUpperLayer(BaseLayer pUpperLayer) { //upper is ip
		// 3,4,5,6,7Layer
		this.p_aUpperLayer.add(nUpperLayerCount++,pUpperLayer);
		
	}
	/**
	 * 2,3계층 사이에 ArpLayer가 관여함 
	 */
	@Override
	public void SetUpperUnderLayer(BaseLayer pUULayer) {
		this.SetUpperLayer(pUULayer); //Ip를 위  layer로
		pUULayer.SetUnderLayer(this); //Ip밑을 arp layer로
		
		
	}
	private Arp_Message getMessage() {
		return message;
	}
	private void setMessage(Arp_Message message) {
		this.message = message;
	}
	private int getnUpperLayerCount() {
		return nUpperLayerCount;
	}
	private void setnUpperLayerCount(int nUpperLayerCount) {
		this.nUpperLayerCount = nUpperLayerCount;
	}
	private String getpLayerName() {
		return pLayerName;
	}
	private void setpLayerName(String pLayerName) {
		this.pLayerName = pLayerName;
	}
	private BaseLayer getP_UnderLayer() {
		return p_UnderLayer;
	}
	private void setP_UnderLayer(BaseLayer p_UnderLayer) {
		this.p_UnderLayer = p_UnderLayer;
	}
	private ArrayList<BaseLayer> getP_aUpperLayer() {
		return p_aUpperLayer;
	}
	private void setP_aUpperLayer(ArrayList<BaseLayer> p_aUpperLayer) {
		this.p_aUpperLayer = p_aUpperLayer;
	}
	private ArrayList<ArrayList<byte[]>> getCacheTable() {
		return cacheTable;
	}
	private void setCacheTable(ArrayList<ArrayList<byte[]>> cacheTable) {
		this.cacheTable = cacheTable;
	}
	private ArrayList<ArrayList<byte[]>> getProxyCacheTable() {
		return proxyCacheTable;
	}
	private void setProxyCacheTable(ArrayList<ArrayList<byte[]>> proxyCacheTable) {
		this.proxyCacheTable = proxyCacheTable;
	}
	private static LayerManager getM_LayerMgr() {
		return m_LayerMgr;
	}
	private static void setM_LayerMgr(LayerManager m_LayerMgr) {
		ARPLayer.m_LayerMgr = m_LayerMgr;
	}
	private int getState() {
		return state;
	}
	private void setState(int state) {
		this.state = state;
	}
	
	
	
		
	
	
}
