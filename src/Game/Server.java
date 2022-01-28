package Game;
import java.awt.*;
import java.io.*;
import javax.swing.*;
import java.net.*;
import java.util.*;

public class Server extends JFrame implements Runnable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int playerNum = 0;
	private JPanel JP = new JPanel();
	private Font font = new Font("黑体",Font.BOLD,18);
	private ServerSocket ss;
	private Socket s;
	private JudgeThread jt;
	private ArrayList<JudgeThread> clients = new ArrayList<JudgeThread>();
	public Server() throws Exception
	{
		JP.setLayout(null);
		JP.setBackground(Color.lightGray);
		this.setTitle("服务器");
		this.setAlwaysOnTop(true);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.add(JP);
		this.setVisible(true);
		ss = new ServerSocket(6666);
		new Thread(this).start();
	}
	public void run()
	{
		try {
			while(true)
			{
				setSize(playerNum*220, 250);
				SetLocation.toLeft(this);
				s = ss.accept();
				playerNum++;
				jt = new JudgeThread(s);
				clients.add(jt);
				jt.start();
			}
		} catch (Exception e) {}
	}
	class JudgeThread extends Thread
	{
		private int num = playerNum;
		private Socket s;
		private String lives;
		public JudgeThread(Socket s)
		{
			this.s = s;
		}
		private PrintStream ps;
		private BufferedReader br;
		private JLabel JTname = new JLabel();
		private JLabel JTlives = new JLabel();
		private String str;
		public void run()
		{
			JTname.setSize(150,30);
			JTname.setFont(font);
			JTlives.setSize(150,30);
			JTlives.setFont(font);
			JTname.setLocation(190*num-180, 50);
			JTlives.setLocation(190*num-180, 100);
			JP.add(JTname);
			JP.add(JTlives);
			JP.repaint();
			try {
				br = new BufferedReader(new InputStreamReader(s.getInputStream()));
				ps = new PrintStream(s.getOutputStream());
				str = br.readLine();
				JTname.setText("玩家："+str);   //先接受玩家姓名和得分
				lives = br.readLine();
				JTlives.setText("剩余生命："+ lives);
				if(playerNum == 1)
				{
					ps.println("wait");			//一个人连接时等待
				}
				else
				{
					for (JudgeThread JT : clients) {
							JT.ps.println("start");  //两个人连接后开始游戏
						}
				}
				while(true)
				{
					str = br.readLine();
					if(str.equals("0"))
					{
						for (JudgeThread JT : clients) {
							if(JT == this)
							{
								JT.ps.println("lose");
								playerNum--;
							}
							else
							{
								JT.ps.println("win");
								playerNum--;
							}
						}
						clients.removeAll(clients);
						setSize(playerNum*220, 250);
						JP.removeAll();
						JP.repaint();
					}
					else if(str.equals("wrongWord"))
					{
						for (JudgeThread JT : clients) {
							if(JT!=this)
							{
								int i =Integer.parseInt(JT.lives);
								i++;
								JT.ps.println(i);
							}
						}
					}
					else
					{
						lives = str;
						JTlives.setText("剩余生命："+ lives);
					}
				}
			} catch (Exception e) {}
		}
	}
	public static void main(String[] args) throws Exception
	{
		new Server();
	}
}