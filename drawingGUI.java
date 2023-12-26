/* Natalie, Trinity, Yan 
 * CSCI 2113, The George Washington University 
 * 
 * This file handels the GUI components of the project 
 * 
 * 
*/


import javax.swing.*;
import javax.swing.border.Border;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.PrintWriter;

public class drawingGUI extends JFrame {
// Yan 11/27 
    private JTextArea chatArea;
    public JTextField messageField;
    private PrintWriter out;
    public boolean isViewerMode = false;
    public boolean isPressed = false;
    public boolean isConnected;
    public boolean loading;
    public JButton currentColor = new JButton("Currently");
    Color[] colors = {Color.WHITE, new Color(209, 177, 135), new Color(199, 123, 88), new Color(174, 93, 64), new Color(199, 123, 88), new Color(121, 68, 74), new Color(75, 61, 68), new Color(186, 145, 88), new Color(146, 116, 65), new Color(77, 69, 57), new Color(119, 116, 59), new Color(179, 165, 85),  new Color(210, 201, 165), new Color(140, 171, 161), new Color(75, 114, 110), new Color(87, 72, 82), new Color(132, 120, 117), new Color(171, 155, 142)};
    public int color = 1;
    private JTextField chatTextBox = new JTextField(30);
    public int[][] data = new int[64][64];
    int drawCount = 0;
    JButton[][] buttons = new JButton[64][64];

    public drawingClient clientSide;
    public drawingGUI app = this;

    String currentWord; 

    String[] gameWords = {"Swing", "Coat", "Shoe", "Ocean", "Dog", "Mouth", "Milk", "Duck", "Skateboard", "Bird", "Mouse", "Whale", "Jacket", "Shirt", "Hippo", "Beach", "Egg", "Cookie", "Cheese", "Skip", "Drum", "homework", "glue", "eraser", "peace", "panic", "alarm", "far", "comfy", "dripping", "boring", "hot", "cold", "parents", "closet", "laugh", "falling", "sleepover", "calendar", "sunscreen", "panda", "detention", "hair", "ice skating", "afraid", "dictionary", "homerun", "root beer float", "hibernation", "street sweeper", "spitball", "drinking fountain", "imagination", "Angry", "Fireworks", "Pumpkin", "Baby", "Flower", "Rainbow", "Beard", "Flying saucer", "Recycle", "Bible", "Giraffe", "Sand castle", "Bikini", "Glasses", "Snowflake", "Book", "High heel", "Stairs", "Bucket", "Ice cream cone", "Starfish", "Bumble bee", "Igloo", "Strawberry", "Butterfly", "Lady bug", "Sun", "Camera", "Lamp", "Tire", "Cat", "Lion", "Toast", "Church", "Mailbox", "Toothbrush", "Crayon", "Night", "Toothpaste", "Dolphin", "Nose", "Truck", "Egg", "Olympics", "Volleyball", "Eiffel Tower", "Peanut", "half cardboard", "oar", "baby-sitter", "drip", "shampoo", "point", "time machine", "yardstick", "think", "lace darts", "world", "avocado bleach", "shower", "curtain", "extension cord dent", "birthday lap", "sandbox", "bruise", "quicksand", "fog", "gasoline", "pocket", "honk", "sponge", "rim", "bride", "wig", "zipper", "wag", "letter opener", "fiddle", "water buffalo", "pilot", "brand pail", "baguette", "rib mascot", "fireman", "pole zoo sushi", "fizz ceiling", "fan bald", "banister punk", "post office", "season", "Internet", "chess", "puppet", "chime", "ivy"};
    JLabel keyWord;
    JLabel timeLeft;
    JLabel time;
    private JTextField username;
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            drawingGUI gui = new drawingGUI(); 
            gui.setVisible(true); 
    
        });
    }
    //Yan 12/03
     private class ColorActionListener implements ActionListener {
        private String color;
        drawingGUI area;
        int indx;
        public ColorActionListener(int indx, drawingGUI area) {
            this.area = area;
            this.indx = indx;
            
        }
    
       @Override
        public void actionPerformed(ActionEvent e) {
            System.out.println(Integer.toString(area.color) + " button clicked no change");
            area.color = indx;
            if(indx == 0)
            {
                area.currentColor.setForeground(Color.BLACK);
            }
            area.currentColor.setBackground(colors[indx]);
            System.out.println(Integer.toString(area.color) + " button clicked");
       }
    }

    public drawingGUI() {
        initUI();
        initChatUI();
    }

    private void initUI() {
        setTitle("Pixelnary");

        JPanel drawingArea = new JPanel();
        drawingArea.setLayout(new GridLayout(64, 64)); 
        createArray(drawingArea);
        add(drawingArea, BorderLayout.CENTER);
       

        JPanel colorPanel = new JPanel();
        
        Border emptyBorder = BorderFactory.createEmptyBorder(4,4,4,4);
        currentColor.setBackground(colors[color]);
        currentColor.setForeground(Color.WHITE);
        currentColor.setOpaque(true);
        currentColor.setBorder(emptyBorder);
 
        int count = 0;
        for (Color color : this.colors) {
            JButton colorButton = new JButton();
            colorButton.setOpaque(true);
            colorButton.setBorder(emptyBorder);
            colorButton.setBackground(color);
            colorButton.addActionListener(new ColorActionListener(count, app));
            colorButton.setPreferredSize(new Dimension(15, 15));
            colorPanel.add(colorButton);
            count++;
        }
        //Natalie 12/10

        colorPanel.add(currentColor);
        add(colorPanel, BorderLayout.SOUTH);


    // Yan 12/02
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setPreferredSize(new Dimension(100, 60)); 
        timeLeft = new JLabel("");
        buttonPanel.add(timeLeft);
        time = new JLabel("");
        buttonPanel.add(time);
        JLabel user = new JLabel("Set username: ");
        buttonPanel.add(user); 
        username = new JTextField();
        username.setText("Unknown");
        buttonPanel.add(username); 

        keyWord = new JLabel("Join to Play!");
        buttonPanel.add(keyWord);

        
        JButton joinServerButton = new JButton("Join Server");
        joinServerButton.setPreferredSize(new Dimension(90, 50)); 
        joinServerButton.setFocusable(false);
        buttonPanel.add(joinServerButton); 

        //Natalie 12/9
        //join server 
        joinServerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(joinServerButton.getText().equals("Join Server")){
                    joinServerButton.setText("Leave Server");
                    username.setEditable(false);
                    loading = true;
                    clientSide = new drawingClient(app);
                    isConnected = true;
        
                }else{
                    joinServerButton.setText("Join Server");
                    username.setEditable(true);
                    isConnected = false;
                    loading = false;
                    clientSide.close();
                    chatArea.setText("");
                }
            }
        });


        add(buttonPanel, BorderLayout.NORTH);
        setSize(700, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

    }
      // Yan 12/08
      private void initChatUI() {
        chatArea = new JTextArea(3, 3);
        chatArea.setEditable(false);

        messageField = new JTextField(5);
        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        JPanel chatPanel = new JPanel(new BorderLayout());
        chatPanel.add(new JScrollPane(chatArea), BorderLayout.CENTER);

        JPanel inputPanel = new JPanel();
        inputPanel.add(messageField);
        inputPanel.add(sendButton);
        chatPanel.add(inputPanel, BorderLayout.SOUTH);

        add(chatPanel, BorderLayout.EAST);
    }
    //Yan 12/10
    public void sendMessage() {
        String message = messageField.getText();
        //out != null &&
        if ( message != null && !message.isEmpty() && isViewerMode) {
            System.out.println("Sent message: " + message);
            //out.println(message);
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    chatArea.append("[" + username.getText() + "] " + message + "\n");
                }
            });
            if(isConnected && isViewerMode)
            {
                System.out.println("Message sent to server");
                String[] msg = {"[" + username.getText() + "] " + message};
                clientSide.writeMessage(msg);
            }
        }
        messageField.setText(""); 
    }

    public void sendMessage(String msg) {
        String message = msg;
        //out != null &&
        if ( message != null && !message.isEmpty()) {
            System.out.println("Sent message: " + message);
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    chatArea.append( message + "\n");
                }
            });
        }
        messageField.setText("");   
    }

    public void labelUpdate(int mode){
        if(mode == 0)
        {
            keyWord.setText("Try To Guess The Word!");
        }
        else if(mode == 1)
        {
            keyWord.setText("Keyword is " + currentWord);
        }
        else if(mode == 2)
        {
            keyWord.setText("Join to Play!");
        }
        else if(mode == 3)
        {
            timeLeft.setText("Time: ");
        }
        else if(mode == 4)
        {
            timeLeft.setText("");
            time.setText("");
        }
    }
    
    public void timeUpdate(int time){
        this.time.setText(Integer.toString(time));
    }
    

    //Trinity
    public void createArray(JPanel panel)
    {
        drawingGUI area = this;
        for(int i = 0; i < buttons.length; i++)
        {
            for(int j = 0; j < buttons[0].length; j++)
            {
                //create and add each button element
                JButton button = new JButton();
                Border emptyBorder = BorderFactory.createEmptyBorder(4,4,4,4);
                button.setOpaque(true);
                button.setBorder(emptyBorder);

                //Setting the button name to its position to be used later
                button.setName(Integer.toString(i) + "," + Integer.toString(j));
                IsDragged d = new IsDragged(area);
                IsClicked c = new IsClicked(area);
                button.addMouseListener(d);
                button.addActionListener(c);
                button.setBackground(Color.WHITE);
                buttons[i][j] = button; 
                panel.add(button);
            }
        }
    }
    //To be used only when not the active drawer
    public void updateButtons(int color, int xCord, int yCord)
    {
        buttons[xCord][yCord].setBackground(colors[color]);
    }

    public synchronized void clearScreen()
    {
        for(int i = 0; i < data.length ; i++)
        {
            for(int j = 0; j < data[0].length; j++)
            {
                updateButtons(0, i, j);
            }
        }
        drawCount = 0;
    }

    public void updateData(int color, int row, int col)
    {
        data[row][col] = color;
    }

    public void showWinMessage(String message) {
        keyWord.setText("Word was guessed correctly!");
        JOptionPane.showMessageDialog(this, message, "Game Over", JOptionPane.INFORMATION_MESSAGE);
    }
    /* 
    public void setDrawingMode(boolean isDrawingMode) {
        this.isDrawingMode = isDrawingMode;
        chatTextBox.setEnabled(!isDrawingMode);
    }
    */
    private class Clear implements ActionListener{
        drawingGUI area;
        public Clear(drawingGUI area)
        {
            this.area = area;
        }
        @Override
        public void actionPerformed(ActionEvent e)
        {
            if (!isViewerMode) {
                for(int i = 0; i < drawCount; i++)
                {
                    clearScreen();
                }
                drawCount = 0;
            }
        }
  }
    private class IsClicked implements ActionListener{
        drawingGUI area;
        public IsClicked(drawingGUI area)
        {
            this.area = area;
        }
        @Override
        public void actionPerformed(ActionEvent e)
        {
            if (!isViewerMode && !loading) {
                
                JButton btn = (JButton)e.getSource();
                String name = btn.getName();
                String[] cords = name.split(",");
                int r = Integer.parseInt(cords[0]);
                int c = Integer.parseInt(cords[1]);
                updateData(area.color, r, c);
                //output to server
                if (isConnected)
                {
                    area.clientSide.writeToServer(color,r,c);
                }
                //System.out.println("Color: " + Integer.toString(color));
                btn.setBackground(colors[area.color]); 
                area.updateData(r, c, area.color);
            }
        }
  }

  private class IsDragged implements MouseListener{
        drawingGUI area;
        public IsDragged(drawingGUI area)
        {
            this.area = area;
        }
        @Override
        public void mousePressed(MouseEvent e)
        {
            isPressed = true;
        }
        @Override
        public void mouseEntered(MouseEvent e)
        {
            //System.out.println("MouseEntered triggered: " + isPressed);
            if(isPressed && !isViewerMode && !loading)
            {
                JButton btn = (JButton)e.getSource();
                String name = btn.getName();
                String[] cords = name.split(",");
                int r = Integer.parseInt(cords[0]);
                int c = Integer.parseInt(cords[1]);
                updateData(area.color, r, c);
                if (isConnected)
                {
                    area.clientSide.writeToServer(area.color,r,c);
                }
            
                //System.out.println("Is clicked");
                btn.setBackground(colors[area.color]); 
                area.updateData(r, c, area.color);
            }
        }
        @Override
        public void mouseExited(MouseEvent e)
        {

        }
         @Override
        public void mouseReleased(MouseEvent e)
        {
            //System.out.println("Is Released");
            isPressed = false;
        }
         @Override
        public void mouseClicked(MouseEvent e)
        {

        }
    }
}

  




  