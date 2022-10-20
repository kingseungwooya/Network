package chat_file;
import java.awt.Color;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.jnetpcap.PcapIf;

public class ARPDlg extends JFrame implements BaseLayer {

   public int nUpperLayerCount = 0;
   public String pLayerName = null;
   public BaseLayer p_UnderLayer = null;
   public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>(); //전송과정에서 필요한 변수들 선언 및 기본값으로 초기화

   public static LayerManager m_LayerMgr = new LayerManager();

   private JTextField ChattingWrite;
   private JTextField PathWrite;
   private JTextField dstIpWrite;
   private JTextField proxyDeviceWrite;
   private JTextField proxyIpWrite;
   private JTextField proxyMacWrite;
   private JTextField GArpAddressWrite;

   Container contentPane;

   JTextArea ChattingArea;
   JTextArea fileArea;
   JTextArea srcMacAddress;
   JTextArea srcIpAddress;
   JTextArea cacheArea;
   JTextArea proxyArpArea;

   JLabel lblsrc;
   JLabel lbldst;
   JLabel dstIpLabel;
   JLabel proxyDevice;
   JLabel proxyIp;
   JLabel proxyMac;
   JLabel GArpAddress;

   JButton Setting_Button;
   JButton Chat_send_Button;
   JButton File_send_Button;
   JButton openFileButton;
   JButton itemDeleteButton;
   JButton allDeleteButton;
   JButton dstIpSendButton;
   JButton proxyAddButton;
   JButton proxyDeleteButton;
   JButton GArpSendButton;

   static JComboBox<String> NICComboBox;

   int adapterNumber = 0;
   byte[] srcIPNumber, dstIPNumber, srcMacNumber;
   String Text;
   JProgressBar progressBar;

   File file;
   
   private ArrayList<ArrayList<byte[]>> cacheTable = new ArrayList<ArrayList<byte[]>>();
   
   public static void main(String[] args) {

      ////////////////

      m_LayerMgr.AddLayer(new NILayer("NI"));
      m_LayerMgr.AddLayer(new EthernetLayer("Ethernet"));
      m_LayerMgr.AddLayer(new ARPLayer("ARP"));
      m_LayerMgr.AddLayer(new IPLayer("IP"));
      m_LayerMgr.AddLayer(new TCPLayer("TCP"));
      // m_LayerMgr.AddLayer(new ChatAppLayer("ChatApp"));
      // m_LayerMgr.AddLayer(new FileAppLayer("FileApp"));
      m_LayerMgr.AddLayer(new ARPDlg("GUI"));
      m_LayerMgr.ConnectLayers(" NI ( *Ethernet ( *ARP ( *IP ( *TCP ( *GUI ) ) ) ) )");
      ///////////////////
   } //레이어 매니저 사용해서 각 레이어를 등록하고 계층의 순서대로 등록

   class setAddressListener implements ActionListener { //Setting에 해당하는 버튼Action 
      @Override
      public void actionPerformed(ActionEvent e) {

         if (e.getSource() == Setting_Button) {

            if (Setting_Button.getText() == "Reset") { //reset에 대한 신호를 받음
               srcMacAddress.setText(""); //텍스트를 모두 공백으로 초기화
               srcIpAddress.setText("");
               Setting_Button.setText("Setting"); //리셋버튼이 setting버튼으로 바뀜
               srcMacAddress.setEnabled(true); //reset을 누름으로서 다시 src와 맥 주소를 적을 수 있게 확성화 함
               srcIpAddress.setEnabled(true);
            } else {
               byte[] MacAddress = new byte[6]; //ARP헤더에서의 MAC, IP주소의 길이
               byte[] IpAddress = new byte[4];

               String srcMac = srcMacAddress.getText(); //사용자가 적은 주소를 얻어와서 저장
               String srcIP = srcIpAddress.getText();
               System.out.println("srcMacAddress : " + srcMac); //출력
               System.out.println("srcIPAddress : " + srcIP);

               String[] byte_srcMac = srcMac.split("-");
               for (int i = 0; i < 6; i++) {
                  MacAddress[i] = (byte) Integer.parseInt(byte_srcMac[i], 16);
               } //맥주소를 '-'기준으로 쪼갠 뒤 저장한다. 맥주소는 6바이트이므로 저장할 배열 길이도 6

               String[] byte_srcIp = srcIP.split("\\.");
               for (int i = 0; i < 4; i++) {
                  IpAddress[i] = (byte) Integer.parseInt(byte_srcIp[i]);
               } //아이피주소를 '-'기준으로 쪼갠 뒤 저장한다. 아이피주소는 4바이트이므로 저장할 배열 길이도 4이다

               srcIPNumber = IpAddress;
               srcMacNumber = MacAddress;
               
               String[] dstMac = {"ff","ff","ff","ff","ff","ff"}; //주소지 맥주소를 모르므로 일단 ff-ff-... -ff로 적어둔다
               byte[] dstMacAddress = new byte[6];
               
               for (int i = 0; i < 6; i++) {
                  dstMacAddress[i] = (byte) Integer.parseInt(dstMac[i], 16);
               }
               //레이어 매니저를 통해 레이어를 얻어온 후 각 레이어에 Src주소, 도착지의 Mac주소 등을 전달한다
               ((EthernetLayer)m_LayerMgr.GetLayer("Ethernet")).SetEnetSrcAddress(MacAddress);
               ((EthernetLayer)m_LayerMgr.GetLayer("Ethernet")).SetEnetDstAddress(dstMacAddress);
               
               ((ARPLayer)m_LayerMgr.GetLayer("ARP")).SetArpSrcAddress(MacAddress); //출발지 맥주소 설정 
               ((ARPLayer)m_LayerMgr.GetLayer("ARP")).SetArpDstAddress(dstMacAddress);
               ((ARPLayer)m_LayerMgr.GetLayer("ARP")).SetIpSrcAddress(IpAddress);
               System.out.println("ARPDlg에서 IPAddress는? " + Byte.toUnsignedInt(IpAddress[2]) +"."+Byte.toUnsignedInt(IpAddress[3]));
               
               ((NILayer) m_LayerMgr.GetLayer("NI")).SetAdapterNumber(adapterNumber);

               Setting_Button.setText("Reset"); //setting을 마침으로서 다시 사용자가 reset할 수 있도록 버튼이 reset으로 바뀜
               srcMacAddress.setEnabled(false); //setting이 끝났으므로 맥주소와 아이피주소에 대한 설정 비활성화
               srcIpAddress.setEnabled(false);

            }
         }
         // basic ARP 전송
         if (e.getSource() == dstIpSendButton) {
            if (dstIpSendButton.getText() == "Send") {//작성한 주소를 Send머튼이 눌릴 때 얻어옴
               String dstIP = dstIpWrite.getText();
               cacheArea.append(dstIP); 
               cacheArea.append("  ??-??-??-??-??-??"); //아직 맥주소를 모르기 때문에 다음과 같이 append한다. 
               cacheArea.append("  Incomplete" + "\n");
               byte[] dstIPAddress = new byte[4]; //주소를 세팅할 때와 같이 '.'기준으로 끊어 저장.
               String[] byte_dstIP = dstIP.split("\\.");
               for (int i = 0; i < 4; i++) { // 헤더에서의 dstIP주소 크기가 4바이트이므로 크기 4의 배열에 저장
                  dstIPAddress[i] = (byte) Integer.parseInt(byte_dstIP[i], 10);
               }
               dstIPNumber = dstIPAddress;
               ((TCPLayer) m_LayerMgr.GetLayer("TCP")).send(srcIPNumber, dstIPNumber); //바로 아래 계층인 TCP레이어에 주소 넘겨줌
               
            }
         }
         // proxy ARP 전송
         if (e.getSource() == proxyAddButton) { //Proxy ARP Entry에 해당하는 Button Action
            //proxy Add 
            if (proxyAddButton.getText() == "Add") {
               String proxyDevice = proxyDeviceWrite.getText(); //사용자가 작성한 Device, Ip, Mac을 각각 텍스트박스에서 읽어옴
               String proxyIP = proxyIpWrite.getText();
               String proxyMac = proxyMacWrite.getText();
               proxyArpArea.append("Interface0"); //proxyArpArea에 맥주소 및 아이피주소 나타나게 함
               proxyArpArea.append("  " + proxyIP);
               proxyArpArea.append("  " + proxyMac + "\n");
               
               byte[] proxyInterfaceByte = new byte[1]; // IP, Interfase, Mac주소의 각 크기에 맞는 배열 생성
               byte[] proxyIpByte = new byte[4];
               byte[] proxyMacByte = new byte[6];
               String[] ip_split = proxyIP.split("\\."); // Ip주소는 '.'을 기준으로 1바이트씩 4자리가 있음
               for (int i = 0; i < 4; i++) { //따라서 4번 끊어서 저장
                  proxyIpByte[i] = (byte) Integer.parseInt(ip_split[i], 10);
               }
               
               String[] mac_split = proxyMac.split("-"); // Ip주소는 '.'을 기준으로 1바이트씩 6자리가 있음
               for (int i = 0; i < 6; i++) { //따라서 6번 끊어서 저장
                  proxyMacByte[i] = (byte) Integer.parseInt(mac_split[i], 16);
               }
               
               proxyInterfaceByte[0] = (byte)Integer.parseInt("1"); //인터페이스 활성화
               ((ARPLayer)m_LayerMgr.GetLayer("ARP")).addProxyTable(proxyInterfaceByte, proxyIpByte, proxyMacByte); //ARP레이어에 얻은 정보를 전달한다
            }
            //proxy Delete 
            else if(proxyAddButton.getText() == "Delete") {
               //Delete 구현 
            }
         }
         
         //GARP 전송 
         if (e.getSource() == GArpSendButton) { //Gartuitous ARP에 해당하는 Button Action
            String garp = GArpAddressWrite.getText(); // 입력한 주소를 읽어옴
            byte[] garpByte = new byte[6];
            
            String[] garp_split = garp.split("-");
            for (int i = 0; i < 6; i++) {
               garpByte[i] = (byte) Integer.parseInt(garp_split[i], 16);
            }
            String[] dstMac = {"ff","ff","ff","ff","ff","ff"}; // ARP하기 전에는 맥주소를 모른다. 그래서 왼쪽과 같은 임시적 초기화
            byte[] dstMacAddress = new byte[6];
            
            for (int i = 0; i < 6; i++) {
               dstMacAddress[i] = (byte) Integer.parseInt(dstMac[i], 16);//주소 끊어서 저장하고 초기값 설정 과정, 위의 다른 함수들과 같음
            }
            
            //((EthernetLayer)m_LayerMgr.GetLayer("Ethernet")).SetEnetSrcAddress(garpByte);
            ((EthernetLayer)m_LayerMgr.GetLayer("Ethernet")).SetEnetDstAddress(dstMacAddress);
            
            ((ARPLayer)m_LayerMgr.GetLayer("ARP")).SetArpSrcAddress(garpByte);
            ((ARPLayer)m_LayerMgr.GetLayer("ARP")).SetArpDstAddress(dstMacAddress);
            
            ((ARPLayer)m_LayerMgr.GetLayer("ARP")).ARPSend(srcIPNumber, srcIPNumber);
         }// 각 하위 레이어에서 필요로 할 정보를 전달한다.
         
         
         //Chatting send 
         if (e.getSource() == Chat_send_Button) { // Chatting에 해당하는 Button Action
            if (Setting_Button.getText() == "Reset") {
               for (int i = 0; i < 10; i++) {
                  String input = ChattingWrite.getText(); //사용자가 입력한 텍스트 읽어옴
                  ChattingArea.append("[SEND] : " + input + "\n"); //Send라는 문자열을 앞에 달아서 채팅화면에 출력
                  byte[] bytes = input.getBytes();
                  m_LayerMgr.GetLayer("ChatApp").Send(bytes, bytes.length); //채팅이므로 두가지 Application 계층 중 ChatApp에 전달
                  if (m_LayerMgr.GetLayer("GUI").Receive()) { // 상대로부터 답신을 받음
                     input = Text;
                     ChattingArea.append("[RECV] : " + input + "\n"); //RECV라는 문자열을 앞에 달아서 채팅화면에 출력
                     continue;
                  }
                  break;
               }
            } else {
               JOptionPane.showMessageDialog(null, "Address Configuration Error"); //주소 입력에 문제가 있는 경우
            }
         }
         if (e.getSource() == openFileButton) { // file trasfer에 해당하는 Button Action
            FileNameExtensionFilter filter = new FileNameExtensionFilter("txt", "txt"); //txt확장자만 필터링 함
            JFileChooser chooser = new JFileChooser(); 
            chooser.setFileFilter(filter);
            int ret = chooser.showOpenDialog(null); //보낼 파일 선택 
            if (ret == JFileChooser.APPROVE_OPTION) {
               String filePath = chooser.getSelectedFile().getPath();
               fileArea.setText(filePath);
               File_send_Button.setEnabled(true); //send버튼 활성화
               file = chooser.getSelectedFile();

            }

         }
         //File send 
         if (e.getSource() == File_send_Button) { //FileApp layer로 파일을 보냄
            ((FileAppLayer) m_LayerMgr.GetLayer("FileApp")).setAndStartSendFile();
            File_send_Button.setEnabled(false); //send버튼 비활성화 
         }
      }

   }

   public ARPDlg(String pName) {
      pLayerName = pName;

      setTitle("Packet_Send_Test");
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      setBounds(250, 250, 1000, 700);
      contentPane = new JPanel();
      ((JComponent) contentPane).setBorder(new EmptyBorder(5, 5, 5, 5));
      setContentPane(contentPane);
      contentPane.setLayout(null);

      // ARP Cache panel
      JPanel arpCachePanel = new JPanel();
      arpCachePanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "ARP Cache",
            TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
      arpCachePanel.setBounds(10, 5, 370, 371);
      contentPane.add(arpCachePanel);
      arpCachePanel.setLayout(null);

      JPanel arpCacheEditorPanel = new JPanel();
      arpCacheEditorPanel.setBounds(10, 15, 350, 230);
      arpCachePanel.add(arpCacheEditorPanel);
      arpCacheEditorPanel.setLayout(null);

      cacheArea = new JTextArea();
      cacheArea.setEditable(false);
      cacheArea.setBounds(0, 0, 350, 220);
      arpCacheEditorPanel.add(cacheArea);// chatting edit

      itemDeleteButton = new JButton("Item Delete");
      itemDeleteButton.setBounds(70, 250, 100, 30);

      allDeleteButton = new JButton("All Delete");
      allDeleteButton.setBounds(200, 250, 100, 30);
      /* add Action Listener for delete button */
      arpCachePanel.add(itemDeleteButton);
      arpCachePanel.add(allDeleteButton);

      dstIpLabel = new JLabel("IP_Addr");
      dstIpLabel.setBounds(15, 300, 100, 20);
      arpCachePanel.add(dstIpLabel);

      dstIpWrite = new JTextField();
      dstIpWrite.setBounds(70, 300, 200, 20);// 249
      arpCachePanel.add(dstIpWrite);
      dstIpWrite.setColumns(10);// target ip address writing area
      dstIpSendButton = new JButton("Send");
      dstIpSendButton.addActionListener(new setAddressListener());
      dstIpSendButton.setBounds(285, 300, 70, 20);
      arpCachePanel.add(dstIpSendButton);

      // proxy arp entry panel
      JPanel proxyArpPanel = new JPanel();
      proxyArpPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Proxy Arp Entry",
            TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
      proxyArpPanel.setBounds(380, 5, 350, 370);
      contentPane.add(proxyArpPanel);
      proxyArpPanel.setLayout(null);

      JPanel proxyEditorPanel = new JPanel();// proxy editor panel
      proxyEditorPanel.setBounds(5, 15, 330, 160);
      proxyArpPanel.add(proxyEditorPanel);
      proxyEditorPanel.setLayout(null);

      proxyArpArea = new JTextArea();
      proxyArpArea.setEditable(false);
      proxyArpArea.setBounds(5, 5, 420, 150);
      proxyEditorPanel.add(proxyArpArea);// proxy arp entry

      JPanel proxyInputPanel = new JPanel();
      proxyInputPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "",
              TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
      proxyInputPanel.setBounds(10, 200, 320, 150);
      proxyInputPanel.setLayout(null);
      proxyArpPanel.add(proxyInputPanel);

      proxyDevice = new JLabel("Device");
      proxyDevice.setBounds(20, 10, 60, 20);
      proxyInputPanel.add(proxyDevice);

      proxyIp = new JLabel("IP_Addr");
      proxyIp.setBounds(20, 40, 60, 20);
      proxyInputPanel.add(proxyIp);

      proxyMac = new JLabel("Mac_Addr");
      proxyMac.setBounds(20, 70, 60, 20);
      proxyInputPanel.add(proxyMac);

      proxyDeviceWrite = new JTextField();
      proxyDeviceWrite.setBounds(100, 10, 200, 20);
      proxyInputPanel.add(proxyDeviceWrite);

      proxyIpWrite = new JTextField();
      proxyIpWrite.setBounds(100, 40, 200, 20);
      proxyInputPanel.add(proxyIpWrite);

      proxyMacWrite = new JTextField();
      proxyMacWrite.setBounds(100, 70, 200, 20);
      proxyInputPanel.add(proxyMacWrite);

      proxyAddButton = new JButton("Add");
      proxyAddButton.setBounds(70, 100, 80, 30);
      proxyDeleteButton = new JButton("Delete");
      proxyDeleteButton.setBounds(180, 100, 80, 30);
      proxyInputPanel.add(proxyAddButton);
      proxyInputPanel.add(proxyDeleteButton);
      
      proxyAddButton.addActionListener(new setAddressListener());
      proxyDeleteButton.addActionListener(new setAddressListener());
      
      // gratuitous panel
      JPanel gratuitousPanel = new JPanel();
      gratuitousPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Gratuitous ARP",
            TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
      gratuitousPanel.setBounds(730, 5, 250, 200);
      contentPane.add(gratuitousPanel);
      gratuitousPanel.setLayout(null);

      GArpAddress = new JLabel("H/W address");
      GArpAddress.setBounds(10, 30, 100, 10);
      GArpAddressWrite = new JTextField();
      GArpAddressWrite.setBounds(20, 60, 220, 20); 
      GArpSendButton = new JButton("전송");
      GArpSendButton.setBounds(70, 100, 100, 30);
      gratuitousPanel.add(GArpAddress);
      gratuitousPanel.add(GArpAddressWrite);
      gratuitousPanel.add(GArpSendButton);
      
      GArpSendButton.addActionListener(new setAddressListener());
      
      // setting panel
      JPanel settingPanel = new JPanel();
      settingPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Address",
            TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
      settingPanel.setBounds(730, 200, 250, 270); //2, 380
      contentPane.add(settingPanel);
      settingPanel.setLayout(null);

      JPanel sourceAddressPanel = new JPanel();
      sourceAddressPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
      sourceAddressPanel.setBounds(50, 96, 190, 20); //3, 170
      settingPanel.add(sourceAddressPanel);
      sourceAddressPanel.setLayout(null);

      lblsrc = new JLabel("Mac");
      lblsrc.setBounds(10, 90, 190, 20);
      settingPanel.add(lblsrc);

      srcMacAddress = new JTextArea();
      srcMacAddress.setBounds(2, 2, 190, 20);
      sourceAddressPanel.add(srcMacAddress);// 자신의 mac address

      JPanel IpPanel = new JPanel();
      IpPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
      IpPanel.setBounds(50, 145, 190, 20);
      settingPanel.add(IpPanel);
      IpPanel.setLayout(null);

      lbldst = new JLabel("IP");
      lbldst.setBounds(10, 145, 190, 20);
      settingPanel.add(lbldst);

      srcIpAddress = new JTextArea();
      srcIpAddress.setBounds(2, 2, 190, 20);
      IpPanel.add(srcIpAddress);// 자신의 ip address

      JLabel NICLabel = new JLabel("NIC");
      NICLabel.setBounds(10, 50, 220, 20);
      settingPanel.add(NICLabel);

      NICComboBox = new JComboBox();
      NICComboBox.setBounds(50, 49, 190, 20);
      settingPanel.add(NICComboBox);

      for (int i = 0; ((NILayer) m_LayerMgr.GetLayer("NI")).getAdapterList().size() > i; i++) {
         NICComboBox.addItem(((NILayer) m_LayerMgr.GetLayer("NI")).GetAdapterObject(i).getDescription());
      }

      NICComboBox.addActionListener(new ActionListener() { // Event Listener

         @Override
         public void actionPerformed(ActionEvent e) { //address setting panel에서 NIC콤보박스를 누르면 선택가능한 리스트를 보여준다
            // TODO Auto-generated method stub

            adapterNumber = NICComboBox.getSelectedIndex();
            System.out.println("Index: " + adapterNumber);
            try {
               srcMacAddress.setText("");
               srcMacAddress.append(get_MacAddress(((NILayer) m_LayerMgr.GetLayer("NI"))
                     .GetAdapterObject(adapterNumber).getHardwareAddress()));

            } catch (IOException e1) {
               // TODO Auto-generated catch block
               e1.printStackTrace();
            }
         }
      });

      try {// Init MAC Address
         srcMacAddress.append(get_MacAddress(
               ((NILayer) m_LayerMgr.GetLayer("NI")).GetAdapterObject(adapterNumber).getHardwareAddress()));
      } catch (IOException e1) {
         // TODO Auto-generated catch block
         e1.printStackTrace();
      }
      ;
      // chatting panel
      JPanel chattingPanel = new JPanel();
      chattingPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "chatting",
            TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
      chattingPanel.setBounds(10, 380, 360, 276);
      contentPane.add(chattingPanel);
      chattingPanel.setLayout(null);

      JPanel chattingEditorPanel = new JPanel();// chatting write panel
      chattingEditorPanel.setBounds(10, 15, 340, 180);
      chattingPanel.add(chattingEditorPanel);
      chattingEditorPanel.setLayout(null);

      ChattingArea = new JTextArea();
      ChattingArea.setEditable(false);
      ChattingArea.setBounds(0, 0, 340, 210);
      chattingEditorPanel.add(ChattingArea);// chatting edit

      JPanel chattingInputPanel = new JPanel();// chatting write panel
      chattingInputPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
      chattingInputPanel.setBounds(10, 230, 250, 20);
      chattingPanel.add(chattingInputPanel);
      chattingInputPanel.setLayout(null);

      ChattingWrite = new JTextField();
      ChattingWrite.setBounds(2, 2, 250, 20);// 249
      chattingInputPanel.add(ChattingWrite);
      ChattingWrite.setColumns(10);// writing area

      // file panel
      JPanel fileTransferPanel = new JPanel();// file panel
      fileTransferPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "file transfer",
            TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
      fileTransferPanel.setBounds(380, 380, 350, 90);
      contentPane.add(fileTransferPanel);
      fileTransferPanel.setLayout(null);

      JPanel fileEditorPanel = new JPanel();// chatting write panel
      fileEditorPanel.setBounds(10, 20, 330, 60);
      fileTransferPanel.add(fileEditorPanel);
      fileEditorPanel.setLayout(null);

      fileArea = new JTextArea();
      fileArea.setEditable(false);
      fileArea.setBounds(0, 5, 250, 20);
      fileEditorPanel.add(fileArea);// chatting edit

      openFileButton = new JButton("File...");
      openFileButton.setBounds(260, 5, 70, 20);
      openFileButton.addActionListener(new setAddressListener());
      fileEditorPanel.add(openFileButton);

      this.progressBar = new JProgressBar(0, 100);
      this.progressBar.setBounds(0, 40, 250, 20);
      this.progressBar.setStringPainted(true);
      fileEditorPanel.add(this.progressBar);

      File_send_Button = new JButton("전송");
      File_send_Button.setBounds(260, 40, 70, 20);
      fileEditorPanel.add(File_send_Button);
      File_send_Button.addActionListener(new setAddressListener());
      File_send_Button.setEnabled(false);

      Setting_Button = new JButton("Setting");// setting
      Setting_Button.setBounds(80, 180, 100, 20);
      Setting_Button.addActionListener(new setAddressListener());
      settingPanel.add(Setting_Button);// setting

      Chat_send_Button = new JButton("Send");
      Chat_send_Button.setBounds(270, 230, 80, 20);
      Chat_send_Button.addActionListener(new setAddressListener());
      chattingPanel.add(Chat_send_Button);// chatting send button

      setVisible(true);

   }

   public File getFile() {
      return this.file;
   }

   public String get_MacAddress(byte[] byte_MacAddress) { //맥주소를 반환

      String MacAddress = "";
      for (int i = 0; i < 6; i++) {
         MacAddress += String.format("%02X%s", byte_MacAddress[i], (i < MacAddress.length() - 1) ? "" : "");
         if (i != 5) {
            MacAddress += "-";
         }
      }

      System.out.println("present MAC address: " + MacAddress);
      return MacAddress;
   }

   public boolean Receive(byte[] input) { //받은 채팅 메시지를 채팅창에 출력 
      if (input != null) {
         byte[] data = input;
         Text = new String(data);
         ChattingArea.append("[RECV] : " + Text + "\n");
         return false;
      }
      return false;
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
      // nUpperLayerCount++;
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

   // cache table setting
   // ip , ethernet , status(0,1)
   public void setArpCache(ArrayList<ArrayList<byte[]>> cacheTable) { //캐시테이블 리스트를 전달받음
      this.cacheTable = cacheTable;
      cacheArea.setText("");
      //byte[] ipAddressByte = new byte[4];
      //byte[] macAddressByte = new byte[6];
      System.out.println("set arp cache");

      for(int i=0; i<cacheTable.size(); i++) { //모든 리스트 내 원소에 대해 
         byte[] ip_byte = cacheTable.get(i).get(0);
         byte[] mac_byte = cacheTable.get(i).get(1);
         byte[] status_byte = cacheTable.get(i).get(2);
         
         String ipByte1 = Integer.toString(Byte.toUnsignedInt(ip_byte[0])); //ip주소 출력
         String ipByte2 = Integer.toString(Byte.toUnsignedInt(ip_byte[1]));
         String ipByte3 = Integer.toString(Byte.toUnsignedInt(ip_byte[2]));
         String ipByte4 = Integer.toString(Byte.toUnsignedInt(ip_byte[3]));
         
         String macByte1 = Integer.toHexString(Byte.toUnsignedInt(mac_byte[0])); //mac주소 출력
         String macByte2 = Integer.toHexString(Byte.toUnsignedInt(mac_byte[1]));
         String macByte3 = Integer.toHexString(Byte.toUnsignedInt(mac_byte[2]));
         String macByte4 = Integer.toHexString(Byte.toUnsignedInt(mac_byte[3]));
         String macByte5 = Integer.toHexString(Byte.toUnsignedInt(mac_byte[4]));
         String macByte6 = Integer.toHexString(Byte.toUnsignedInt(mac_byte[5]));
         
         cacheArea.append(ipByte1+"."+ipByte2+"."+ipByte3+"."+ipByte4);
         cacheArea.append("  "+macByte1+"-"+macByte2+"-"+macByte3+"-"+macByte4+"-"+macByte5+"-"+macByte6);
         System.out.println(ipByte1+"."+ipByte2+"."+ipByte3+"."+ipByte4);
         System.out.println("  "+macByte1+"-"+macByte2+"-"+macByte3+"-"+macByte4+"-"+macByte5+"-"+macByte6);

         if (byte2ToInt(status_byte[0], status_byte[1])==1) { //complete여부 출력
            cacheArea.append("  complete" + "\n");
         }
         else {
            cacheArea.append("  Incomplete" + "\n");
         }
         
      }
      
   }
   
   // cache table setting
   // interface , ip, mac 
//   public void setProxyCache(ArrayList<ArrayList<byte[]>> cacheTable) {
//      this.cacheTable = cacheTable;
//      cacheArea.setText("");
//      //byte[] ipAddressByte = new byte[4];
//      //byte[] macAddressByte = new byte[6];
//      System.out.println("set arp cache");
//
//      for(int i=0; i<cacheTable.size(); i++) {
//         byte[] interface_byte = cacheTable.get(i).get(0);
//         byte[] ip_byte = cacheTable.get(i).get(1);
//         byte[] mac_byte = cacheTable.get(i).get(2);
//         
//         String ipByte1 = Integer.toString(Byte.toUnsignedInt(ip_byte[0]));
//         String ipByte2 = Integer.toString(Byte.toUnsignedInt(ip_byte[1]));
//         String ipByte3 = Integer.toString(Byte.toUnsignedInt(ip_byte[2]));
//         String ipByte4 = Integer.toString(Byte.toUnsignedInt(ip_byte[3]));
//         
//         String macByte1 = Integer.toHexString(Byte.toUnsignedInt(mac_byte[0]));
//         String macByte2 = Integer.toHexString(Byte.toUnsignedInt(mac_byte[1]));
//         String macByte3 = Integer.toHexString(Byte.toUnsignedInt(mac_byte[2]));
//         String macByte4 = Integer.toHexString(Byte.toUnsignedInt(mac_byte[3]));
//         String macByte5 = Integer.toHexString(Byte.toUnsignedInt(mac_byte[4]));
//         String macByte6 = Integer.toHexString(Byte.toUnsignedInt(mac_byte[5]));
//         
//         cacheArea.append("Interface"+Byte.toUnsignedInt(interface_byte[0]));
//         cacheArea.append(ipByte1+"."+ipByte2+"."+ipByte3+"."+ipByte4);
//         cacheArea.append("  "+macByte1+"-"+macByte2+"-"+macByte3+"-"+macByte4+"-"+macByte5+"-"+macByte6);
//         System.out.println(ipByte1+"."+ipByte2+"."+ipByte3+"."+ipByte4);
//         System.out.println("  "+macByte1+"-"+macByte2+"-"+macByte3+"-"+macByte4+"-"+macByte5+"-"+macByte6);
//
//      }
//   }
   
   private int byte2ToInt(byte value1, byte value2) {
        return (int)((value1 << 8) | (value2));
    }
}