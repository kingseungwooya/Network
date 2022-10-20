package chat_file;
import java.util.ArrayList;

public class IPLayer implements BaseLayer {
	public int nUpperLayerCount = 0;
	public String pLayerName = null;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();
	
	private class _IP_ADDR {
		private byte[] addr = new byte[4];

		public _IP_ADDR() {
			this.addr[0] = (byte) 0x00;
			this.addr[1] = (byte) 0x00;
			this.addr[2] = (byte) 0x00;
			this.addr[3] = (byte) 0x00;
		}
	}

	private class _IP_HEADER {
		byte ip_verlen;  //ip version => IPv4:4
		byte ip_tos;     //type of service
		byte[] ip_len;   //total packet length
		byte[] ip_id;    //datagram id
		byte[] ip_fragoff;  //fragment offset
		byte ip_ttl;     //time to live in gateway hops
		byte ip_proto;   //IP protocol
		byte[] ip_cksum; //header checksum
		_IP_ADDR ip_src; //IP address of source
		_IP_ADDR ip_dst; //IP address of destination
		byte[] ip_data;  //variable length data

		public _IP_HEADER() {
			this.ip_src = new _IP_ADDR();
			this.ip_dst = new _IP_ADDR();
			this.ip_len = new byte[2];
			this.ip_id = new byte[2];
			this.ip_fragoff = new byte[2];
			this.ip_cksum = new byte[2];
			this.ip_data = null;
		}
	}

	_IP_HEADER m_sHeader = new _IP_HEADER();

	public IPLayer(String pName) {
		// super(pName);
		// TODO Auto-generated constructor stub
		pLayerName = pName;
		ResetHeader();
	}
	
	public void ResetHeader() {
		for (int i = 0; i < 4; i++) { //reset 4byte size data
			m_sHeader.ip_dst.addr[i] = (byte) 0x00;
			m_sHeader.ip_src.addr[i] = (byte) 0x00;
		}
		for (int i=0; i<2; i++) { //reset 2byte size data
			m_sHeader.ip_len[i] = (byte) 0x00;
			m_sHeader.ip_id[i] = (byte) 0x00;
			m_sHeader.ip_fragoff[i] = (byte) 0x00;
			m_sHeader.ip_cksum[i] = (byte) 0x00;
		}//reset 1byte size data
		m_sHeader.ip_verlen = (byte) 0x00;;
		m_sHeader.ip_tos = (byte) 0x00;;
		m_sHeader.ip_ttl = (byte) 0x00;;
		m_sHeader.ip_proto = (byte) 0x00;;
		m_sHeader.ip_data = null;
	}
	
	public _IP_ADDR GetDst_IPAddress() {
		return m_sHeader.ip_dst;
	}

	public _IP_ADDR GetSrc_IPAddress() {
		return m_sHeader.ip_src;
	}
	
	public void SetDst_IPAddress(byte[] input) {
		for (int i = 0; i < 4; i++) {
			m_sHeader.ip_dst.addr[i] = input[i];
		}
	}

	public void SetSrc_IPAddress(byte[] input) {
		for (int i = 0; i < 4; i++) {
			m_sHeader.ip_src.addr[i] = input[i];
		}
	}
	
	public void send(byte[] src, byte[] dst) {
		this.SetSrc_IPAddress(src);  //get IP Address from upper layer.. and setting
		this.SetDst_IPAddress(dst);
		((ARPLayer) this.GetUnderLayer()).send(src, dst);
	}
	
	
	@Override
	public String GetLayerName() {
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
		if (nindex < 0 || nindex > nUpperLayerCount || nUpperLayerCount < 0)
			return null;
		return p_aUpperLayer.get(nindex);
	}
	@Override
	public void SetUnderLayer(BaseLayer pUnderLayer) {
		if (pUnderLayer == null)
			return;
		this.p_UnderLayer = pUnderLayer;
	}
	@Override
	public void SetUpperLayer(BaseLayer pUpperLayer) {
		if (pUpperLayer == null)
			return;
		this.p_aUpperLayer.add(nUpperLayerCount++, pUpperLayer);
	}
	@Override
	public void SetUpperUnderLayer(BaseLayer pUULayer) {
		this.SetUpperLayer(pUULayer);
		pUULayer.SetUnderLayer(this);
	}
}