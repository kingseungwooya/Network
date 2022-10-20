package chat_file;

import java.util.ArrayList;

/**
 IP 헤더에 필요한 정보를 담아서 송신
 Destination IP, Source IP and etc.
 수신 된 패킷의 source IP가 자신의 것이면, 버림
 수신 된 패킷의 destination IP가 자신의 것이면 TCP Layer로 데이터를 전달하
고, 아니면 버림
실제 위의 구현은 ChatApp, FileApp 추가구현 시 필요. (채팅 ,파일 전달)
 * @author ksw_0
 */
public class TCPLayer implements BaseLayer {
  private class TCPLayer_HEADER{
		byte[] tcp_sport;// source port (
		byte[] tcp_dport;// destination port (
		byte[] tcp_seq;// sequence number (
		byte[] tcp_ack;// acknowledged sequence (
		byte[] tcp_offset;// no use (
		byte[] tcp_flag;// control flag (
		byte[] tcp_window;// no use (
		byte[] tcp_cksum;// check sum (
		byte[] tcp_urgptr;// no use (
		byte[] padding; //(4byte)
		byte[] tcp_data; // data part
		
		public TCPLayer_HEADER() {
			this.tcp_sport = new byte[2];
			this.tcp_dport = new byte[2];
			this.tcp_seq = new byte[4];
			this.tcp_ack = new byte[4];
			this.tcp_offset = new byte[1];
			this.tcp_flag = new byte[1];
			this.tcp_window = new byte[2];
			this.tcp_cksum = new byte[2];
			this.tcp_urgptr = new byte[2];
			this.padding = new byte[4]; 
		}
		

	}
	
	
	TCPLayer_HEADER sendCase = new TCPLayer_HEADER();
	TCPLayer_HEADER recvCase = new TCPLayer_HEADER();
	
	
    public int nUpperLayerCount = 0;
    public int nUnderLayerCount = 0;
    
    public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();
    public BaseLayer p_UnderLayer;
    
    public BaseLayer GetUnderLayer() {
        if (p_UnderLayer == null)
            return null;
        return p_UnderLayer;
    }
    
    public void SetUnderLayer(BaseLayer pUnderLayer) {
        if (pUnderLayer == null)
            return;
        this.p_UnderLayer = pUnderLayer;
    }
    
	public BaseLayer GetUpperLayer(int nindex) {
		if (nindex < 0 || nindex > nUpperLayerCount || nUpperLayerCount < 0)
			return null;
		return p_aUpperLayer.get(nindex);
	}
	
    public void SetUpperLayer(BaseLayer pUpperLayer) {
        if (pUpperLayer == null)
            return;
        this.p_aUpperLayer.add(nUpperLayerCount++, pUpperLayer);
    }
   
    /*
    private byte[] intToByte2(int value) {
        byte[] temp = new byte[2];
        temp[0] |= (byte) ((value & 0xFF00) >> 8);
        temp[1] |= (byte) (value & 0xFF);

        return temp;
    }
    */
    
    private int byte2ToInt(byte value1, byte value2) {
        return (int) ((value1 << 8) | (value2));
    }
    
    public byte[] RemoveTCPHeader(byte[] input, int length) {
        byte[] cpyInput = new byte[length - 24];
        System.arraycopy(input, 24, cpyInput, 0, length - 24);
        input = cpyInput;
        return input;
    }
	/**
	 * send수정 ArpDlg에서 srcip와 dstip를 입력받음 
	 */
    public boolean send(byte[] srcIPNumber, byte[]dstIPNumber) {
    	
        ((IPLayer)GetUnderLayer()).send(srcIPNumber,dstIPNumber);
        return true;
    }
    
	
    public boolean Receive(byte[] input) {
    	byte[] data = RemoveTCPHeader(input, input.length);
    	
        int temp_type = input[3];
        
        if(temp_type == (byte)0x90){
        	GetUpperLayer(0).Receive(data);
            return true;
        }
        
        else if(temp_type == (byte)0x91) {
        	GetUpperLayer(1).Receive(data);
        	return true;
        }
        
        else {
        	return false;
        }

    }

	@Override
	public String GetLayerName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void SetUpperUnderLayer(BaseLayer pUULayer) {
		// TODO Auto-generated method stub
		
	}


}
