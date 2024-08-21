import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import javax.swing.JFileChooser;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

public class chat_server extends javax.swing.JFrame {

    static ServerSocket ss; //listen for incoming connections
    static Socket s; //comm with the connected client
    static DataInputStream dis;
    static DataOutputStream dout;
    static File saveDirectory;

    public chat_server() {
        initComponents();
    }

    private void initComponents() {
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        msg_area = new javax.swing.JTextPane();
        msg_txt = new javax.swing.JTextField();
        send_msg = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        send_file = new javax.swing.JButton();
        file_label = new javax.swing.JLabel();
        select_directory = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("CozyCorner - Chatroom");
        getContentPane().setBackground(new java.awt.Color(240, 255, 240));
        setPreferredSize(new java.awt.Dimension(400, 600));
        setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.insets = new java.awt.Insets(5, 5, 5, 5);
        gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;

        jLabel2.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 18));
        jLabel2.setForeground(new java.awt.Color(0, 128, 0));
        jLabel2.setText("CozyCorner - Chatting with Alice");
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        add(jLabel2, gbc);

        msg_area.setEditable(false);
        msg_area.setBackground(new java.awt.Color(220, 255, 220));
        msg_area.setContentType("text/html");
        msg_area.setText("<html><body></body></html>");
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.fill = java.awt.GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 0.8;
        jScrollPane1.setViewportView(msg_area);
        add(jScrollPane1, gbc);

        msg_txt.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 14));
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.8;
        gbc.weighty = 0;
        add(msg_txt, gbc);

        send_msg.setBackground(new java.awt.Color(0, 128, 0));
        send_msg.setForeground(java.awt.Color.WHITE);
        send_msg.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 14));
        send_msg.setText("SEND");
        send_msg.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 128, 0)));
        send_msg.addActionListener(evt -> send_msgActionPerformed(evt));
        gbc.gridx = 1;
        gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.2;
        add(send_msg, gbc);

        file_label.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 12));
        file_label.setForeground(new java.awt.Color(0, 128, 0));
        file_label.setText("No file selected");
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.6;
        add(file_label, gbc);

        send_file.setBackground(new java.awt.Color(0, 128, 0));
        send_file.setForeground(java.awt.Color.WHITE);
        send_file.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 14));
        send_file.setText("Send File");
        send_file.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 128, 0)));
        send_file.addActionListener(evt -> send_fileActionPerformed(evt));
        gbc.gridx = 1;
        gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.4;
        add(send_file, gbc);

        select_directory.setBackground(new java.awt.Color(0, 128, 0));
        select_directory.setForeground(java.awt.Color.WHITE);
        select_directory.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 14));
        select_directory.setText("Select Save Directory");
        select_directory.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 128, 0)));
        select_directory.addActionListener(evt -> select_directoryActionPerformed(evt));
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        add(select_directory, gbc);

        pack();
    }

    private void msg_txtActionPerformed(java.awt.event.ActionEvent evt) {
    }

    //method for sending messages
    private void send_msgActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            String msg = msg_txt.getText(); //get txt
            dout.writeUTF("msg:" + msg);
            appendToPane(msg_area, "<span style='color:green;'><b>Bob: </b>" + msg + "</span><br>");
            msg_txt.setText("");
        } catch (Exception e) {
            e.printStackTrace(); //if an error occurs
        }
    }

    //method for sending files
    private void send_fileActionPerformed(java.awt.event.ActionEvent evt) {
        JFileChooser fileChooser = new JFileChooser(); //standard swing component to select file
        int returnValue = fileChooser.showOpenDialog(null); //pop up for user to select a file
        if (returnValue == JFileChooser.APPROVE_OPTION) { //file selection confirmation
            File selectedFile = fileChooser.getSelectedFile();
            file_label.setText(selectedFile.getName()); //gui is updated via file_label
            try {
                dout.writeUTF("file:" + selectedFile.getName());
                //reading and sending the contents of file
                FileInputStream fis = new FileInputStream(selectedFile); //fis to read the contents of file
                byte[] buffer = new byte[4096]; //buffer to read chunks of file
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) { //return number of bytes read
                    dout.write(buffer, 0, bytesRead);
                }
                fis.close(); //closed to release system resources
                dout.flush();
                //update the msg area on status of the file
                appendToPane(msg_area,
                        "<span style='color:green;'>Bob sent file: " + selectedFile.getName() + "</span><br>");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //save dir
    private void select_directoryActionPerformed(java.awt.event.ActionEvent evt) {
        JFileChooser directoryChooser = new JFileChooser();
        directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnValue = directoryChooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            saveDirectory = directoryChooser.getSelectedFile();
            appendToPane(msg_area, "<span style='color:blue;'>Save directory set to: " + saveDirectory.getAbsolutePath()
                    + "</span><br>");
        }
    }

    //main method
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> new chat_server().setVisible(true));
        try {
            ss = new ServerSocket(1201); //create a serversocket to listen on port
            s = ss.accept(); //accept the connection
            dis = new DataInputStream(s.getInputStream());
            dout = new DataOutputStream(s.getOutputStream());
            String msgin = "";
            while (!msgin.equals("exit")) {
                msgin = dis.readUTF();
                if (msgin.startsWith("msg:")) {
                    appendToPane(msg_area,
                            "<span style='color:blue;'><b>Alice: </b>" + msgin.substring(4) + "</span><br>");
                } else if (msgin.startsWith("file:")) { //check is message is a file
                    String fileName = msgin.substring(5); //get the name of file
                    File receivedFile;
                    if (saveDirectory != null) {
                        receivedFile = new File(saveDirectory, "received_" + fileName);
                    } else {
                        receivedFile = new File("received_" + fileName);
                    }
                    FileOutputStream fos = new FileOutputStream(receivedFile);
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = dis.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead);
                        if (bytesRead < buffer.length)
                            break; // End of file
                    }
                    fos.close();
                    appendToPane(msg_area, "<span style='color:blue;'><b>Alice sent file: </b>" + fileName +
                            "</span><br>" + "<span style='color:#ff122e;'><i>Saved as: </i>"
                            + receivedFile.getAbsolutePath() + "</span><br>");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void appendToPane(JTextPane tp, String msg) {
        HTMLDocument doc = (HTMLDocument) tp.getDocument();
        HTMLEditorKit editorKit = (HTMLEditorKit) tp.getEditorKit();
        try {
            editorKit.insertHTML(doc, doc.getLength(), msg, 0, 0, null);
        } catch (BadLocationException | IOException e) {
            e.printStackTrace();
        }
    }

    //GUI components
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private static javax.swing.JTextPane msg_area;
    private javax.swing.JTextField msg_txt;
    private javax.swing.JButton send_msg;
    private javax.swing.JButton send_file;
    private javax.swing.JButton select_directory;
    private javax.swing.JLabel file_label;
}
