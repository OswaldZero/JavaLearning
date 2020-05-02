package MyVim;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.zip.InflaterInputStream;

public class JVim extends JFrame {
    private JTextArea jTextArea=new JTextArea();
    private JTextField jLabel =new JTextField();
    ArrayList<String> mode=new ArrayList<>();
    Queue<Integer> queue=new LinkedList<Integer>();
    char mark[]=new char[2];
    private File file;

    public static void main(String[] args) throws IOException {
        JVim jVim=new JVim(args);
    }
    public JVim(String []args) throws IOException {
        //设置Frame名字
        setTitle("MyVim");

        //给模式添加成员
        mode.add("normal mode");
        mode.add("insert mode");

        //给标记队列添加元素
        queue.offer((Integer)0);
        queue.offer((Integer)0);

        //设置Lable
        jLabel.setText(mode.get(0));

        //判断类型及填充
        if(args.length==0){
            //建立默认文档
            //注意这里windows会出问题,因为分隔符和转义的原因
            File parentFile=new File(System.getProperty("user.dir"));
            file=new File(parentFile,"a.txt");
        }else{
            //相对路径于绝对路径判断
            if(!args[1].startsWith("/")){
                File fileParent=new File(System.getProperty("user.dir"));
                file=new File(fileParent,args[0]);
            }else{
                file=new File(args[0]);
            }

            //填入text
            InputStream inputStream=new FileInputStream(file);
            InputStreamReader inputStreamReader=new InputStreamReader(inputStream);
            jTextArea.read(inputStreamReader,"fill the text");
            inputStreamReader.close();
        }

        //设置一些属性
        jTextArea.setEditable(false);
        jLabel.setEditable(false);
        jTextArea.grabFocus();

        //添加焦点监视器
        jTextArea.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                jTextArea.getCaret().setVisible(true);
            }

            @Override
            public void focusLost(FocusEvent e) {
                jTextArea.getCaret().setVisible(false);
            }
        });

        jLabel.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                jTextArea.getCaret().setVisible(true);
            }

            @Override
            public void focusLost(FocusEvent e) {
                jTextArea.getCaret().setVisible(false);
            }
        });

        //添加键盘监视器
        jTextArea.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e) {
                int CaretPositionLine= 0;
                int lineStartOffset=0;
                int lineEndOffset=0;
                int lineLength=0;
                try {
                    //获取光标所在行行号
                    CaretPositionLine = jTextArea.getLineOfOffset(jTextArea.getCaretPosition());

                    //获取光标所在行的首偏移量
                    lineStartOffset=jTextArea.getLineStartOffset(CaretPositionLine);

                    //获取光标所在行的尾偏移量
                    lineEndOffset=jTextArea.getLineEndOffset(CaretPositionLine);

                    //获取光标所在行的长度
                    lineLength=lineEndOffset-lineStartOffset+1;

                } catch (BadLocationException ex) {
                    ex.printStackTrace();
                }

                //当为正常模式时
                if (jLabel.getText().equals(mode.get(0))){
                    jTextArea.setEditable(false);
                    jTextArea.grabFocus();

                    //出队
                    queue.poll();
                    //进队
                    queue.offer((Integer)(e.getKeyCode()));


                    //有 i I a A o O六种方式进入编辑模式

                    //为i时进入当前光标所在的前一个处输入
                    if(e.getKeyCode()==KeyEvent.VK_I) {
                        jLabel.setText(mode.get(1));
                        if(jTextArea.getCaretPosition()!=lineStartOffset){
                            jTextArea.setCaretPosition(jTextArea.getCaretPosition()-1);
                        }
                    }

                    //为I时进入行首输入
                    if(e.getKeyCode()==KeyEvent.VK_I && e.isShiftDown())
                    {
                        jLabel.setText(mode.get(1));
                        jTextArea.setCaretPosition(lineStartOffset);
                    }

                    //a 为从目前光标所在输入
                    if(e.getKeyCode()==KeyEvent.VK_A){
                        jLabel.setText(mode.get(1));
                    }

                    //A为从光标所在行的最后一个字符处开始输入
                    if (e.getKeyCode()==KeyEvent.VK_A && e.isShiftDown()){
                        jLabel.setText(mode.get(1));
                        jTextArea.setCaretPosition(lineEndOffset);
                    }
                    //这个要注意,window下为/r/n,Linux下为/n,mac下为/r
                    //o为到下一行
                    if(e.getKeyCode()==KeyEvent.VK_O){
                        jLabel.setText(mode.get(1));
                        jTextArea.setCaretPosition(lineEndOffset);
                        jTextArea.replaceSelection("\n");
                        jTextArea.setCaretPosition(jTextArea.getCaretPosition()-1);
                    }
                    //O为到该行上一行输入
                    if(e.getKeyCode()==KeyEvent.VK_O && e.isShiftDown()){
                        jLabel.setText(mode.get(1));
                        jTextArea.setCaretPosition(lineStartOffset);
                        jTextArea.replaceSelection("\n");
                        jTextArea.setCaretPosition(jTextArea.getCaretPosition()-1);
                    }


                    //当有:的时候进入命令模式
                    if (e.getKeyCode()==KeyEvent.VK_SEMICOLON && e.isShiftDown()){
                        jLabel.setText(":");
                    }


                    //下面是正常的光标移动操作

                    //当键入为h键,并且插入符不在该行第一个位置时
                    if (e.getKeyCode()==KeyEvent.VK_H && (jTextArea.getCaretPosition()!=lineStartOffset)){
                        jTextArea.setCaretPosition(jTextArea.getCaretPosition()-1);
                    }

                    //当键击为l键,并且插入符不在改行最后一个位置时
                    if (e.getKeyCode()==KeyEvent.VK_L && (jTextArea.getCaretPosition()!=lineEndOffset)){
                        jTextArea.setCaretPosition(jTextArea.getCaretPosition()+1);
                    }


                    //当键击为j键,并且下一行不为未编辑行时
                    if (e.getKeyCode()==KeyEvent.VK_J && (jTextArea.getRows()!=CaretPositionLine+1)){
                        //获取下一行的行号
                        int nextLine=CaretPositionLine+1;

                        //获取下一行的首偏移量
                        int nextLineStartOffset=0;
                        try {
                            nextLineStartOffset=jTextArea.getLineStartOffset(nextLine);
                        } catch (BadLocationException ex) {
                            ex.printStackTrace();
                        }

                        //获取下一行的尾偏移量
                        int nextLineEndOffset= 0;
                        try {
                            nextLineEndOffset = jTextArea.getLineEndOffset(nextLine);
                        } catch (BadLocationException ex) {
                            ex.printStackTrace();
                        }

                        //获取下一行的长度
                        int nextLineLength=nextLineEndOffset-nextLineEndOffset+1;

                        //获取光标处到其所在行行首的长度
                        int CaretLength=jTextArea.getCaretPosition()-lineStartOffset+1;


                        if(nextLineLength>=CaretLength){
                            jTextArea.setCaretPosition(jTextArea.getCaretPosition()+lineLength);
                        }else{
                            jTextArea.setCaretPosition(nextLineEndOffset);
                        }
                    }

                    //当敲击为k键,并且上一行不为首行时
                    if(e.getKeyCode()==KeyEvent.VK_K && CaretPositionLine!=0){
                        //获取上一行的行号
                        int priorLine=CaretPositionLine-1;

                        //获取上一行的首偏移量
                        int priorLineStartOffset=0;
                        try {
                            priorLineStartOffset=jTextArea.getLineStartOffset(priorLine);
                        } catch (BadLocationException ex) {
                            ex.printStackTrace();
                        }

                        //获取上一行的尾偏移量
                        int priorLineEndOffset=0;
                        try{
                            priorLineEndOffset=jTextArea.getLineEndOffset(priorLine);
                        } catch (BadLocationException ex) {
                            ex.printStackTrace();
                        }

                        //获取上一行的长度
                        int priorLineLength=priorLineEndOffset-priorLineStartOffset+1;

                        //获取光标处到其所在行行首的长度
                        int CaretLength=jTextArea.getCaretPosition()-lineStartOffset+1;

                        if(CaretLength>priorLineLength){
                            jTextArea.setCaretPosition(priorLineEndOffset);
                        }else {
                            jTextArea.setCaretPosition(priorLineStartOffset+CaretLength);
                        }
                    }

                    //当键击为x时
                    if(e.getKeyCode()==KeyEvent.VK_X){
                        if(!(jTextArea.getCaretPosition()==lineStartOffset && jTextArea.getCaretPosition()==lineEndOffset)){
                            if(jTextArea.getCaretPosition()!=lineEndOffset){
                                jTextArea.select(jTextArea.getCaretPosition(),jTextArea.getCaretPosition()+1);
                                jTextArea.replaceSelection("");
                            }else if(jTextArea.getCaretPosition()!=lineStartOffset){
                                jTextArea.select(jTextArea.getCaretPosition(),jTextArea.getCaretPosition()-1);
                                jTextArea.replaceSelection("");
                                jTextArea.setCaretPosition(jTextArea.getCaretPosition()-1);
                            }else{

                            }
                        }
                    }

                    //当键击为dd时
                    if(e.getKeyCode()==KeyEvent.VK_D && queue.peek()==KeyEvent.VK_D){
                        jTextArea.select(lineStartOffset,lineEndOffset);
                        jTextArea.replaceSelection("");
                        jTextArea.setCaretPosition(lineStartOffset);
                    }



                }
                else  if (jLabel.getText().equals(mode.get(1))){

                    //把文本设为可编辑状态
                    jTextArea.setEditable(true);

                    //若为ESC就回到命令模式
                    if(e.getKeyCode()==KeyEvent.VK_ESCAPE){
                        jLabel.setText(mode.get(0));
                    }
                }
                else{
                    jLabel.grabFocus();
                    jLabel.setEditable(true);
                }
            }

            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });

        jLabel.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode()==KeyEvent.VK_ENTER){
                    jTextArea.grabFocus();
                    jLabel.setEditable(false);
                    char arr[]=jLabel.getText().toCharArray();
                    if(arr.length==2 && arr[1]=='w' && arr[0]==':'){
                        try {
                            FileWriter fileWriter=new FileWriter(file);
                            BufferedWriter bufferedWriter=new BufferedWriter(fileWriter);
                            bufferedWriter.write(jTextArea.getText());
                            bufferedWriter.close();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                    if(arr.length==3 && arr[0]==':'){

                        if(arr[1]=='w' && arr[2]=='q'){
                            try {
                                FileWriter fileWriter=new FileWriter(file);
                                BufferedWriter bufferedWriter=new BufferedWriter(fileWriter);
                                bufferedWriter.write(jTextArea.getText());
                                bufferedWriter.close();
                                System.exit(0);
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }

                        if(arr[1]=='q' && arr[2]=='!'){
                            System.exit(0);
                        }
                    }
                    jLabel.setText(mode.get(0));

                }

                if(e.getKeyCode()==KeyEvent.VK_ESCAPE){
                    jTextArea.grabFocus();
                    jLabel.setEditable(false);
                    jLabel.setText(mode.get(0));
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });

        add(BorderLayout.CENTER,jTextArea);
        add(BorderLayout.SOUTH, jLabel);
        setSize(800,800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }
}
