package chat_file;

import java.util.ArrayList;

/**
 * 구현해야함
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
	//ARP message 28byte 이므로 각 위치별 바이트배열 위치당 값들 할당
	//Arp 객체를 byte배열에 값을 다 할당 해줌      
	public byte[] ArpToByte(Arp_Message msg, int length) {
		byte[] packet = new byte[28];
		//opcode 정수 변환시 2이면 request 1이면 send, default는 send로 함 
		if (byteToInt(msg.opcode[0], msg.opcode[1]) == 2) {
			Mac_Address temp = msg.sender_Mac;
			msg.target_Mac = temp;
			msg.sender_Mac = temp;
		}

		for (int i = 0; i < 2; i++) {
			packet[i] = msg.hardware_type[i];
			packet[i + 2] = msg.protocol_type[i];
			packet[i + 6] = msg.opcode[i];
		}

		packet[4] = msg.hard_size; // 고정 
		packet[5] = msg.protocol_size; //고정 

		for (int i = 0; i < 6; i++) {
			packet[i + 8] = msg.sender_Mac.address[i];
			packet[i + 18] = msg.target_Mac.address[i];
		}
		for (int i = 0; i < 4; i++) {
			packet[i + 14] = msg.sender_Ip.address[i];
			packet[i + 24] = msg.target_Ip.address[i];
		}
		return packet;
	}
	private byte[] intToByte(int value) {
		byte[] temp = new byte[2];
		temp[0] |= (byte) ((value & 0xFF00) >> 8);
		temp[1] |= (byte) (value & 0xFF);

		return temp;
	}

	private int byteToInt(byte value1, byte value2) {
		return (int) ((value1 << 8) | (value2));
	}
	/**\
	 *  ip주소로만 찾기 
	 * @param ip_src 출발지 ip 주소
	 * @param ip_dst 도착지 ip 주소
	 */
	
	public void send(byte[] ip_src, byte[] ip_dst){ 
		frame.prottype = intToByte2(0x0800);
		this.SetIpSrcAddress(ip_src);
		this.SetIpDstAddress(ip_dst);
		byte[] bytes = ObjToByte(frame, 28);
		((EthernetLayer) this.GetUnderLayer()).ARPSend(bytes, 28);
		return false;
		
	}
	public void receive(){
		
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
