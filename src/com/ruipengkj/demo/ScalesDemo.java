package com.ruipengkj.demo;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.TooManyListenersException;

import javax.comm.CommDriver;
import javax.comm.CommPortIdentifier;
import javax.comm.PortInUseException;
import javax.comm.SerialPort;
import javax.comm.SerialPortEvent;
import javax.comm.SerialPortEventListener;
import javax.comm.UnsupportedCommOperationException;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class ScalesDemo implements SerialPortEventListener, ActionListener {

	public InputStream inputStream; // 串口输入流
 
	private JTextArea textArea; // 数据显示文本框
	private JComboBox<ComboBoxItem> portChoice; // 读取电脑的串口列表
	private JButton button; // 打开或者关闭串口按钮

	public ScalesDemo() {
		// 创建窗口
		JFrame frame = new JFrame();
		frame.setSize(new Dimension(400, 500)); // 设置窗口大小
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());

		// 创建打开/关闭操作按钮
		button = new JButton("打开串口");
		button.addActionListener(this); // 绑定点击事件
		// 创建选择端口下拉框
		portChoice = new JComboBox<ComboBoxItem>();
		portChoice.addItem(new ComboBoxItem("请选择端口", null));
		this.listPort();

		// 创建文件域
		textArea = new JTextArea();

		// 布局界面，将下拉框，按钮和文本框分布到窗口
		JPanel pane1 = new JPanel();
		pane1.add(portChoice);
		pane1.add(button);
		JScrollPane pane2 = new JScrollPane(textArea);
		frame.add(pane1, BorderLayout.NORTH);
		frame.add(pane2);
		frame.setVisible(true);
	}

	/**
	 * 按钮点击事件
	 */
	public void actionPerformed(ActionEvent event) {
		if (button.getText().equals("打开串口")) {
			open();
		} else {
			if (sPort != null) {
				sPort.close();
				button.setText("打开串口");
			}
		}
	}

	/**
	 * 端口事件类型：
	 * 
	 * BI -通讯中断. 　　 
	 * CD -载波检测. 　　 
	 * CTS -清除发送. 　　 
	 * DATA_AVAILABLE -有数据到达. 　　
	 * DSR-数据设备准备好. 　　 
	 * FE -帧错误. 　　 
	 * OE -溢位错误. 　　 
	 * OUTPUT_BUFFER_EMPTY -输出缓冲区已清空.
	 * PE -奇偶校验错. 
	 * RI -　振铃指示.
	 */
	public void serialEvent(SerialPortEvent event) {
		switch (event.getEventType()) {
		case SerialPortEvent.BI:
		case SerialPortEvent.OE:
		case SerialPortEvent.FE:
		case SerialPortEvent.PE:
		case SerialPortEvent.CD:
		case SerialPortEvent.CTS:
		case SerialPortEvent.DSR:
		case SerialPortEvent.RI:
		case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
			break;
		case SerialPortEvent.DATA_AVAILABLE:
			byte[] readBuffer = new byte[1024];
			try {
				while (inputStream.available() > 0) {
					inputStream.read(readBuffer);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			String text = new String(readBuffer).trim();
			textArea.append(text + "\n");
			break;
		}
	}

	/**
	 * 读取所有端口，放置到下拉框
	 */
	private void listPort() {
		// 加载驱动
		String driverName = "com.sun.comm.Win32Driver";
		CommDriver driver = null;
		try {
			driver = (CommDriver) Class.forName(driverName).newInstance();
			driver.initialize();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// 遍历所有串口放入下拉框
		CommPortIdentifier portId;
		Enumeration<?> en = CommPortIdentifier.getPortIdentifiers();
		while (en.hasMoreElements()) {
			portId = (CommPortIdentifier) en.nextElement();
			// 将类型是串口的端口添加到下拉框中
			if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
				portChoice.addItem(new ComboBoxItem(portId.getName(), portId));
			}
		}
	}
	
	private SerialPort sPort = null;
	/**
	 * 打开端口
	 */
	private void open() {
		// 获取一个要打开的端口CommPortIdentifier对象。
		if (portChoice.getSelectedIndex() == 0) {
			JOptionPane.showMessageDialog(null, "请先选择串口！");
			return;
		}
		ComboBoxItem item = (ComboBoxItem) portChoice.getSelectedItem();
		if (item == null || item.getPort() == null) {
			return;
		}
		CommPortIdentifier portId = item.getPort();
		try {
			// 如果端口未关闭，先执行关闭，避免资源占用
			if (sPort != null) {
				sPort.close();
			}
			// 打开一个端口，等待超时3000毫秒
			sPort = (SerialPort) portId.open("SerialDemo", 3000);
		} catch (PortInUseException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "连接失败，端口正在被使用！");
			return;
		}
		try {
			sPort.setSerialPortParams(9600,// 波特率
					                SerialPort.DATABITS_8,// 数据位数
					                SerialPort.STOPBITS_1, // 停止位
					                SerialPort.PARITY_NONE);// 奇偶位
		} catch (UnsupportedCommOperationException e1) {
			e1.printStackTrace();
		}

		// 打开端口的输入流
		try {
			inputStream = sPort.getInputStream();
		} catch (IOException e) {
			sPort.close();
			e.printStackTrace();
		}
		// 为串行端口添加一个事件侦听器
		try {
			sPort.addEventListener(this);
		} catch (TooManyListenersException e) {
			sPort.close();
		}
		// 有数据通知，触发端口事件
		sPort.notifyOnDataAvailable(true);

		button.setText("关闭串口");
	}

	public static void main(String[] args) {
		new ScalesDemo();
	}

}
