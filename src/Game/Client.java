package Game;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import java.net.*;
import java.util.*;

public class Client extends JFrame implements KeyListener{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JLabel JLEng = new JLabel();
	private JLabel JLCh = new JLabel();
	private JLabel JLUFO = new JLabel();
	private Icon UFO = new ImageIcon("UFO.png");
	private Icon missle = new ImageIcon("missle.png");
	private Icon bullet = new ImageIcon("bullet.png");
	private Icon title = new ImageIcon("title.png");
	private Icon boom = new ImageIcon("boom.png");
	private JLabel JLLife = new JLabel();
	private JLabel JLName = new JLabel();
	private JLabel JLTitle = new JLabel();
	private JLabel JLTitleimg = new JLabel();
	private Image backImage = new ImageIcon("star.png").getImage(); 
	private BackgroundPanel JP = new BackgroundPanel(backImage);
	private Font engFont = new Font("黑体",Font.BOLD,22);
	private Font chFont = new Font("黑体",Font.BOLD,22);
	private Font titleFont = new Font("黑体",Font.BOLD,30);
	private String gameMode;
	private int lives;
	private boolean fallFlag;
	private boolean startFlag;
	private int completeNum;
	private int existingNum;
	private ArrayList<String> words = new ArrayList<String>();
	private ArrayList<String> translations = new ArrayList<String>();
	private String fullWord;
	private String[] temp;
	private String word;
	private String translation;
	private char ch[] = new char[2];
	private int index[] = new int[2];
	private String name;
	private Socket s;
	private PrintStream ps;
	private BufferedReader br;
	private BufferedReader brword = new BufferedReader(new FileReader("EasyWords.txt"));
	private FileWriter FWCom;
	private FileWriter FWunf;
	private FileWriter FWWro;
	public Client() throws Exception{
		//读取单词文件
		while(true)
		{
			fullWord = brword.readLine();
			if(fullWord == null)
			{
				break;
			}
			temp=fullWord.split(" ");
			words.add(temp[0]);
			translations.add(temp[1]);
		}
		JP.setLayout(null);
		JP.setBackground(Color.LIGHT_GRAY);
		this.add(JP);
		this.setTitle("英语六级词汇学习游戏系统");
		this.setSize(1000, 800);
		SetLocation.toCenter(this);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.addKeyListener(this);
		this.setVisible(true);
		displayMenu();	//主界面
		name = JOptionPane.showInputDialog("输入昵称");
		
	}
	public void keyPressed(KeyEvent e) {
		if(startFlag && existingNum<2)  //开始游戏后才能输入，且同时只能存在两个字母
		{
			existingNum++;
			new InputTheard(e).start();
		}
	}
	
	public void keyTyped(KeyEvent e) {}
	public void keyReleased(KeyEvent e){}
	
	public void displayMenu()		//主界面
	{
		lives = 20;
		fallFlag = true;
		completeNum = 0;
		existingNum = 0;
		JButton JBStartSingle = new JButton();
		JButton JBStartMulti = new JButton();
		JButton JBExit = new JButton();
		JLTitleimg.setIcon(title);
		JLTitleimg.setSize(title.getIconWidth(),title.getIconHeight());
		JLTitleimg.setLocation(300, 100);
		JBStartSingle.setFocusable(false);
		JBStartSingle.setText("单人练习");
		JBStartSingle.setFont(chFont);
		JBStartSingle.setSize(150, 30);
		JBStartSingle.setLocation(420, 300);
		JBStartMulti.setFocusable(false);
		JBStartMulti.setText("多人游戏");
		JBStartMulti.setFont(chFont);
		JBStartMulti.setSize(150, 30);
		JBStartMulti.setLocation(420, 370);
		JBExit.setFocusable(false);
		JBExit.setText("退出游戏");
		JBExit.setFont(chFont);
		JBExit.setSize(150, 30);
		JBExit.setLocation(420, 440);
		JP.add(JBStartSingle);
		JP.add(JBStartMulti);
		JP.add(JBExit);
		JP.add(JLTitleimg);
		JP.repaint();
		JBStartSingle.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				gameMode = "single";
				JP.removeAll();
				JP.repaint();
				displayStart();
			}
		});
		JBStartMulti.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				gameMode = "multi";
				try {
					s = new Socket("192.168.0.100",6666);
					br = new BufferedReader(new InputStreamReader(s.getInputStream()));
					ps = new PrintStream(s.getOutputStream());
					ps.println(name);
					ps.println(lives);
				} catch (Exception ex) {}
				new ServerInputThread().start(); //此线程负责接受服务器信息
			}
		});
		JBExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
	}
	
	public void displayStart()  //游戏界面
	{
		JP.removeAll();
		JP.repaint();
		startFlag = true;
		setFocusable(true);
		try {
			FWCom = new FileWriter("completed"+name+".txt",true);
			FWunf = new FileWriter("unfinished"+name+".txt",true);
			FWWro = new FileWriter("wrong"+name+".txt",true);
		} catch (Exception e) {}
		JLLife.setSize(200, 30);
		JLLife.setFont(engFont);
		JLLife.setForeground(Color.white);
		JLLife.setLocation(20,120);
		JLName.setSize(200, 30);
		JLName.setFont(chFont);
		JLName.setForeground(Color.white);
		JLName.setLocation(20, 80);
		JLName.setText("昵称："+name);
		JLEng.setFont(engFont);
		JLEng.setIcon(missle);
		JLEng.setForeground(Color.white);
		JLEng.setSize(missle.getIconWidth(),missle.getIconHeight());
		JLCh.setFont(chFont);
		JLCh.setForeground(Color.white);
		JLUFO.setSize(UFO.getIconWidth(), UFO.getIconHeight());
		JLUFO.setIcon(UFO);
		JP.add(JLEng);
		JP.add(JLCh);
		JP.add(JLUFO);
		JP.add(JLLife);
		JP.add(JLName);
		JP.repaint();
		new UFOMoveThread().start();
		new FallTheard().start();
	}
	
	public void displayWait()
	{
		JP.removeAll();
		JP.repaint();
		JLabel JL = new JLabel();
		JL.setSize(600,50);
		JL.setLocation((getWidth()-JL.getWidth())/2,(getHeight()-JL.getHeight())/2);
		JL.setForeground(Color.white);
		JL.setFont(new Font("黑体",Font.BOLD,42));
		JL.setText("正在等待其他玩家加入......");
		JP.add(JL);
		JP.repaint();
	}
	
	public void showEnd(String str) throws Exception
	{
		JButton JBExit = new JButton();
		JButton JBMenu = new JButton();
		String text;
		switch (str) {
		case "lose":
			text = "你输了，再接再厉吧！";
			break;
		case "win":
			text = "你赢了，不要骄傲哦！";
			break;
		case "end":
			text = "练习结束，继续努力！";
			break;
		default:
			text = "服务器错误";
			break;
		}
		JLTitle.setText(text);
		JLTitle.setSize(400,40);
		JLTitle.setForeground(Color.white);
		JLTitle.setFont(titleFont);
		JLTitle.setLocation(350, 100);
		JBExit.setText("退出游戏");
		JBExit.setFont(chFont);
		JBExit.setSize(150, 30);
		JBExit.setLocation(420, 350);
		JBExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					FWCom.close();
					FWunf.close();
					FWWro.close();
				} catch (IOException e1) {}
				System.exit(0);
			}
		});
		JBMenu.setText("返回主菜单");
		JBMenu.setFont(chFont);
		JBMenu.setSize(150, 30);
		JBMenu.setLocation(420, 300);
		JBMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					FWCom.close();
					FWunf.close();
					FWWro.close();
				} catch (IOException e1) {}
				JP.removeAll();
				JP.repaint();
				displayMenu();
			}
		});
		JP.removeAll();
		JP.add(JBExit);
		JP.add(JBMenu);
		JP.add(JLTitle);
		JP.repaint();
	}
	
	class UFOMoveThread extends Thread
	{
		int x = (getWidth()-JLUFO.getWidth())/2;
		boolean directionFlag;
		public void run()
		{
			while(startFlag)
			{
				if(getWidth()-JLUFO.getLocation().x == JLUFO.getWidth() || JLUFO.getLocation().x == 0)
				{
					directionFlag = !directionFlag;
				}
				if(directionFlag)
				{
					x++;
				}
				else 
				{
					x--;
				}
				JLUFO.setLocation(x, 0);
				try {
					Thread.sleep(10);
				} catch (Exception e) {}
			}
		}
	}
	
	class FallTheard extends Thread{
		private int y;
		private int key;
		private StringBuilder bb = new StringBuilder();
		public void run()
		{
			while(lives > 0 && startFlag)
			{
				completeNum=0;
				y = JLUFO.getHeight();
				key =(int)(Math.random()*words.size());
				bb.delete(0, bb.length());
				word = words.get(key);	//随机读取单词
				fullWord = word;
				translation = translations.get(key);
				bb.append(word);
				index[0] = (int)(Math.random()*word.length());   //随机选两个字母变下划线
				for(index[1] = (int)(Math.random()*word.length());index[1]==index[0];)
				{
					index[1] = (int)(Math.random()*word.length()); //选不同的字母
				}
				if(index[0] > index[1])
				{
					int i = index[0];
					index[0] = index[1]; //小的在前面
					index[1] = i;
				}
				ch[0] =  word.charAt(index[0]);   //保存下划线的位置和原来的字母
				ch[1] = word.charAt(index[1]);
				bb.setCharAt(index[0], '_');
				bb.setCharAt(index[1], '_');
				word = bb.toString();
				JLEng.setLocation(JLUFO.getLocation().x-(JLEng.getWidth()-JLUFO.getWidth())/2,JLUFO.getHeight());
				JLEng.setText(word);
				JLEng.setHorizontalTextPosition(JLabel.CENTER);
				JLCh.setSize(translation.length()*30,30);
				JLCh.setLocation(300,getHeight()-2*JLCh.getHeight());
				JLCh.setText(translation);
				JLLife.setText("分数："+lives);
				fallFlag = true;
				while(fallFlag && (completeNum!=2) && startFlag)  //一直下降直到完成单词或落到底部或游戏结束
				{
					JLEng.setLocation(JLEng.getLocation().x,y);
					y += 1;
					if(JLEng.getLocation().y == getHeight()-2*JLEng.getHeight()) //单词落到底部时
					{
						fallFlag = false;
						try {
							FWunf.write(fullWord+" "+translation+"\n");
						} catch (Exception e) {
						}
						lives--;
						if(gameMode.equals("multi"))
						{
							ps.println(lives);
						}
					}
					try {
						Thread.sleep(10);
					}catch(Exception e){}
				}
			}
			if(gameMode.equals("single"))
			{
				try {
					startFlag = false;
					showEnd("end");
				} catch (Exception e) {}
			}
		}
	}
	
	class InputTheard extends Thread{
		private boolean destoryFlag = false;
		private KeyEvent e;
		private StringBuilder bb = new StringBuilder();
		private double y;
		private double x;
		private double k;
		public InputTheard(KeyEvent e) {
			this.e = e;
		}
		public void run()
		{
			JLabel JL = new JLabel();
			JLabel JLBOOM = new JLabel();
			JL.setFont(new Font("黑体",Font.BOLD,40));
			JL.setForeground(Color.red);
			JL.setIcon(bullet);
			JL.setHorizontalTextPosition(JLabel.CENTER);
			JL.setSize(bullet.getIconWidth(),bullet.getIconHeight());
			JL.setText(e.getKeyChar()+""); //转换成字符串
			JLBOOM.setIcon(boom);
			JLBOOM.setSize(boom.getIconWidth(), boom.getIconHeight());
			y=getHeight()-60;
			x=getWidth()/2;
			JP.add(JL);
			while(!destoryFlag)
			{
				k=-(JLEng.getLocation().getY()-y)/(JLEng.getLocation().getX()+JLEng.getWidth()/2-x);
				y -= 1;
				x += 1/k;		//动态计算，射向单词
				JL.setLocation((int)Math.round(x),(int)Math.round(y)); //四舍五入更流畅
				try {
					Thread.sleep(1);
				}catch(Exception e){}
				if(JL.getLocation().y-JLEng.getLocation().y <= 2) 	//击中单词时判断
				{
					destoryFlag = true;
					existingNum--;
					JP.remove(JL);
					JP.repaint();
					if(e.getKeyChar()==ch[0] && completeNum == 0)
					{
						bb.append(word);
						bb.setCharAt(index[0], ch[0]);
						word=bb.toString();
						JLEng.setText(word);
						completeNum++;
					}
					else if(e.getKeyChar()==ch[1] && completeNum == 1)
					{	
						bb.append(word);
						bb.setCharAt(index[1], ch[1]);
						word=bb.toString();
						JLEng.setText(word);
						completeNum++;
						lives++;
						if(gameMode.equals("multi")) //多人游戏时发送信息
						{
							ps.println(lives);
						}
						try {
							FWCom.write(fullWord+" "+translation+"\n");
						} catch (Exception e) {}
					}
					else
					{
						try {
							FWWro.write(fullWord+" "+translation+"\n");
						} catch (Exception e) {
						}
						lives--;
						if(gameMode.equals("multi"))
						{
							ps.println(lives);
							ps.println("wrongWord");
						}
						fallFlag=false;
						if(lives!=0)
						{
							JLBOOM.setLocation(JLEng.getLocation().x-20,JLEng.getLocation().y-JLBOOM.getHeight()/2);
							JP.add(JLBOOM);
							JP.repaint();
							try {
								sleep(700);
								JP.remove(JLBOOM);
								JP.repaint();
							} catch (Exception e) {}
						}
					}
				}
			}
		}
	}
	class ServerInputThread extends Thread{
		private String str;
		public void run() {
			try {
				while(true)
				{
					str = br.readLine();
					switch (str) {
					case "lose":
						startFlag = false;
						showEnd("lose");
						break;
					case "win":
						startFlag = false;
						showEnd("win");
						break;
					case "wait":
						displayWait();
						break;
					case "start":
						displayStart();
						break;
					default:
						lives = Integer.parseInt(str);
						JLLife.setText("分数："+lives);
						ps.println(lives);
						break;
					}
				}
			}
			catch(Exception e) {}
		}
	}
	public static void main(String[] args) throws Exception {
		new Client();
	}
}