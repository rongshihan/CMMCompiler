package compiler;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.HashMap;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.StyledDocument;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.undo.UndoManager;

import structure.TreeNode;
import util.*;

public class CompilerFrame extends JFrame {
	private static final long serialVersionUID = 14L;
	/* ״̬�� */
	private final static JStatusBar STATUSBAR = new JStatusBar();
	/* ����˵��� */
	private final static JMenuBar MENUBAR = new JMenuBar();
	/* ���幤���� */
	private final static JToolBar TOOLBAR = new JToolBar();
	/* �ļ������ */
	private final static JFileTree FILETREE = new JFileTree(
			new JFileTree.ExtensionFilter("lnk"));
	/* Ĭ������ */
	private final static Font LABELFONT = new Font("��Բ", Font.BOLD, 13);
	/* �༭������ */
	private Font font = new Font("Courier New", Font.PLAIN, 15);
	/* ����̨�ʹ����б����� */
	private Font conAndErrFont = new Font("΢���ź�", Font.PLAIN, 14);
	/* �﷨���������ʾ������ */
	private Font treeFont = new Font("΢���ź�", Font.PLAIN, 12);
	/* �ļ��˵� */
	private static JMenu fileMenu;
	/* �༭�˵� */
	private static JMenu editMenu;
	/* ���в˵� */
	private static JMenu runMenu;
	/* ��ʽ�˵� */
	private static JMenu setMenu;
	/* ���ڲ˵� */
	private static JMenu windowMenu;
	/* �����˵� */
	private static JMenu helpMenu;
	/* ����̨�ʹ�����Ϣ */
	public static JTabbedPane proAndConPanel;
	/* CMM�����ı��༭�� */
	private static JCloseableTabbedPane editTabbedPane;
	private static HashMap<JScrollPane, StyleEditor> map = new HashMap<JScrollPane, StyleEditor>();
	/* ����̨(���������) */
	public static JTextPane consoleArea = new JTextPane();
	/* ������ʾ�� */
	public static JTextArea problemArea = new JTextArea();
	/* ����ʹ򿪶Ի��� */
	private FileDialog filedialog_save, filedialog_load;
	/* Undo������ */
	private final UndoManager undo = new UndoManager();
	private UndoableEditListener undoHandler = new UndoHandler();
	/* �༭���Ҽ��˵� */
	private JPopupMenu popupMenu = new JPopupMenu();
	private JMenuItem item1;
	private JMenuItem item2;
	private JMenuItem item3;
	private JMenuItem item4;
	/* �˵����� */
	private JMenuItem newItem;
	private JMenuItem openItem;
	private JMenuItem saveItem;
	private JMenuItem exitItem;
	private JMenuItem undoItem;
	private JMenuItem redoItem;
	private JMenuItem copyItem;
	private JMenuItem cutItem;
	private JMenuItem pasteItem;
	private JMenuItem allItem;
	private JMenuItem searchItem;
	private JMenuItem deleteItem;
	private JMenuItem lexItem;
	private JMenuItem parseItem;
	private JMenuItem runItem;
	private JMenuItem fontItem;
	private JMenuItem startPageItem;
	private JMenuItem newWindowItem;
	private JMenuItem helpItem;
	private JMenuItem aboutItem;
	/* ��������ť */
	private JButton newButton;
	private JButton openButton;
	private JButton saveButton;
	private JButton runButton;
	private JButton lexButton;
	private JButton parseButton;
	private JButton undoButton;
	private JButton redoButton;
	private JButton copyButton;
	private JButton cutButton;
	private JButton pasteButton;
	private JButton searchButton;
	private JButton fontButton;
	private JButton helpButton;
	private JButton aboutButton;
	/* �ļ������� */
	FileFilter filter = new FileFilter() {
		public String getDescription() {
			return "CMM�����ļ�(*.cmm)";
		}

		public boolean accept(File file) {
			String tmp = file.getName().toLowerCase();
			if (tmp.endsWith(".cmm") || tmp.endsWith(".CMM")) {
				return true;
			}
			return false;
		}
	};
	/* ����Ҫ���ҵ��ַ��� */
	private static String findStr = null;
	/* ��ǰ�ı��༭���ַ��� */
	private static String text = null;
	/* ��ǰѡ����ı���λ�� */
	private static int position;
	/* ���Ҵ��� */
	private static int time = 0;
	/* CMMLexer�ʷ����� */
	private CMMLexer lexer = new CMMLexer();
	/* CMMParser�﷨���� */
	private CMMParser parser;
	/* CMMParser������� */
	private CMMSemanticAnalysis semanticAnalysis;
	/* �ʷ������﷨���������ʾ��� */
	private JTabbedPane tabbedPanel;
	/* �û����� */
	private String userInput;
	/* ����̨���� */
	private static int columnNum;
	/* ����̨���� */
	private static int rowNum;
	/* ����̨������� */
	private static int presentMaxRow;
	private static int[] index = new int[] { 0, 0 };
	private static StyledDocument doc = null;

	/**
	 * ���캯��
	 * 
	 * @param title
	 */
	public CompilerFrame(String title) {
		super();
		setLayout(new BorderLayout());
		setTitle(title);
		setJMenuBar(MENUBAR);

		setDefaultCloseOperation(EXIT_ON_CLOSE);
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			SwingUtilities.updateComponentTreeUI(FILETREE);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// ��ʼ���˵���
		fileMenu = new JMenu("�ļ�(F)");
		editMenu = new JMenu("�༭(E)");
		runMenu = new JMenu("����(R)");
		setMenu = new JMenu("����(S)");
		windowMenu = new JMenu("����(W)");
		helpMenu = new JMenu("����(H)");

		// ���ÿ�ݷ�ʽ
		fileMenu.setMnemonic(KeyEvent.VK_F);
		editMenu.setMnemonic(KeyEvent.VK_E);
		runMenu.setMnemonic(KeyEvent.VK_R);
		setMenu.setMnemonic(KeyEvent.VK_S);
		windowMenu.setMnemonic(KeyEvent.VK_W);
		helpMenu.setMnemonic(KeyEvent.VK_H);

		// ���˵���ӵ��˵���
		MENUBAR.add(fileMenu);
		MENUBAR.add(editMenu);
		MENUBAR.add(runMenu);
		MENUBAR.add(setMenu);
		MENUBAR.add(windowMenu);
		MENUBAR.add(helpMenu);

		// Ϊ�ļ��˵��������
		newItem = new JMenuItem("�� ��", new ImageIcon(getClass().getResource(
				"/images/new.png")));
		newItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
				ActionEvent.CTRL_MASK));
		openItem = new JMenuItem("�� ��", new ImageIcon(getClass().getResource(
				"/images/open.png")));
		openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
				ActionEvent.CTRL_MASK));
		saveItem = new JMenuItem("�� ��", new ImageIcon(getClass().getResource(
				"/images/save.png")));
		saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
				ActionEvent.CTRL_MASK));
		exitItem = new JMenuItem("�� ��", new ImageIcon(getClass().getResource(
				"/images/exit.png")));
		exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E,
				ActionEvent.CTRL_MASK));
		fileMenu.add(newItem);
		fileMenu.add(openItem);
		fileMenu.add(saveItem);
		fileMenu.addSeparator();
		fileMenu.add(exitItem);

		// Ϊ�༭�˵��������
		undoItem = new JMenuItem("��  ��", new ImageIcon(getClass().getResource(
				"/images/undo.png")));
		undoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z,
				ActionEvent.CTRL_MASK));
		redoItem = new JMenuItem("��  ��", new ImageIcon(getClass().getResource(
				"/images/redo.png")));
		redoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,
				ActionEvent.CTRL_MASK));
		copyItem = new JMenuItem("��  ��", new ImageIcon(getClass().getResource(
				"/images/copy.png")));
		copyItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,
				ActionEvent.CTRL_MASK));
		cutItem = new JMenuItem("��  ��", new ImageIcon(getClass().getResource(
				"/images/cut.png")));
		cutItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,
				ActionEvent.CTRL_MASK));
		pasteItem = new JMenuItem("ճ  ��", new ImageIcon(getClass().getResource(
				"/images/paste.png")));
		pasteItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V,
				ActionEvent.CTRL_MASK));
		allItem = new JMenuItem("ȫ  ѡ", new ImageIcon(getClass().getResource(
				"/images/all.png")));
		allItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,
				ActionEvent.CTRL_MASK));
		searchItem = new JMenuItem("��  ��", new ImageIcon(getClass()
				.getResource("/images/search.png")));
		searchItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F,
				ActionEvent.CTRL_MASK));
		deleteItem = new JMenuItem("ɾ  ��", new ImageIcon(getClass()
				.getResource("/images/delete.png")));
		deleteItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D,
				ActionEvent.CTRL_MASK));
		editMenu.add(undoItem);
		editMenu.add(redoItem);
		editMenu.addSeparator();
		editMenu.add(copyItem);
		editMenu.add(cutItem);
		editMenu.add(pasteItem);
		editMenu.add(deleteItem);
		editMenu.add(allItem);
		editMenu.addSeparator();
		editMenu.add(searchItem);

		// Ϊ���в˵��������
		lexItem = new JMenuItem("�ʷ�����", new ImageIcon(getClass().getResource(
				"/images/lex.png")));
		lexItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));
		parseItem = new JMenuItem("�﷨����", new ImageIcon(getClass().getResource(
				"/images/parse.png")));
		parseItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0));
		runItem = new JMenuItem("��    ��", new ImageIcon(getClass().getResource(
				"/images/run.png")));
		runItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
		runMenu.add(lexItem);
		runMenu.add(parseItem);
		runMenu.addSeparator();
		runMenu.add(runItem);

		// Ϊ���ò˵��������
		fontItem = new JMenuItem("�� ��", new ImageIcon(getClass().getResource(
				"/images/font.png")));
		fontItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T,
				ActionEvent.CTRL_MASK));
		setMenu.add(fontItem);

		// Ϊ���ڲ˵��������
		startPageItem = new JMenuItem("��ʼҳ", new ImageIcon(getClass()
				.getResource("/images/startpage.png")));
		startPageItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
				ActionEvent.ALT_MASK));
		newWindowItem = new JMenuItem("�½�����", new ImageIcon(getClass()
				.getResource("/images/window.png")));
		newWindowItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W,
				ActionEvent.CTRL_MASK));
		windowMenu.add(startPageItem);
		windowMenu.add(newWindowItem);

		// Ϊ�����˵��������
		helpItem = new JMenuItem("�� ��", new ImageIcon(getClass().getResource(
				"/images/help.png")));
		helpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
		aboutItem = new JMenuItem("�� ��", new ImageIcon(getClass().getResource(
				"/images/about.png")));
		aboutItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I,
				ActionEvent.CTRL_MASK));
		helpMenu.add(helpItem);
		helpMenu.add(aboutItem);

		// �����Ҽ��˵�
		item1 = new JMenuItem("�� ��    ", new ImageIcon(getClass().getResource(
				"/images/copy.png")));
		item2 = new JMenuItem("�� ��    ", new ImageIcon(getClass().getResource(
				"/images/cut.png")));
		item3 = new JMenuItem("ճ ��    ", new ImageIcon(getClass().getResource(
				"/images/paste.png")));
		item4 = new JMenuItem("ȫ ѡ    ", new ImageIcon(getClass().getResource(
				"/images/all.png")));
		popupMenu.add(item1);
		popupMenu.add(item2);
		popupMenu.add(item3);
		popupMenu.addSeparator();
		popupMenu.add(item4);

		// ������
		newButton = new JButton(new ImageIcon(getClass().getResource(
				"/images/new.png")));
		newButton.setToolTipText("�½�");
		newButton.setBorderPainted(false);
		openButton = new JButton(new ImageIcon(getClass().getResource(
				"/icons/folder.png")));
		openButton.setToolTipText("��");
		openButton.setBorderPainted(false);
		saveButton = new JButton(new ImageIcon(getClass().getResource(
				"/images/save.png")));
		saveButton.setToolTipText("����");
		saveButton.setBorderPainted(false);
		lexButton = new JButton(new ImageIcon(getClass().getResource(
				"/images/lex.png")));
		lexButton.setToolTipText("�ʷ�����");
		lexButton.setBorderPainted(false);
		parseButton = new JButton(new ImageIcon(getClass().getResource(
				"/images/parse.png")));
		parseButton.setToolTipText("�﷨����");
		parseButton.setBorderPainted(false);

		runButton = new JButton(new ImageIcon(getClass().getResource(
				"/icons/play.png")));
		runButton.setToolTipText("����");
		runButton.setBorderPainted(false);

		undoButton = new JButton(new ImageIcon(getClass().getResource(
				"/images/undo.png")));
		undoButton.setToolTipText("����");
		undoButton.setBorderPainted(false);
		redoButton = new JButton(new ImageIcon(getClass().getResource(
				"/images/redo.png")));
		redoButton.setToolTipText("����");
		redoButton.setBorderPainted(false);
		copyButton = new JButton(new ImageIcon(getClass().getResource(
				"/images/copy.png")));
		copyButton.setToolTipText("����");
		copyButton.setBorderPainted(false);
		cutButton = new JButton(new ImageIcon(getClass().getResource(
				"/images/cut.png")));
		cutButton.setToolTipText("����");
		cutButton.setBorderPainted(false);
		pasteButton = new JButton(new ImageIcon(getClass().getResource(
				"/images/paste.png")));
		pasteButton.setToolTipText("ճ��");
		pasteButton.setBorderPainted(false);
		searchButton = new JButton(new ImageIcon(getClass().getResource(
				"/images/search.png")));
		searchButton.setToolTipText("����");
		searchButton.setBorderPainted(false);
		fontButton = new JButton(new ImageIcon(getClass().getResource(
				"/images/font.png")));
		fontButton.setToolTipText("��������");
		fontButton.setBorderPainted(false);
		helpButton = new JButton(new ImageIcon(getClass().getResource(
				"/images/help.png")));
		helpButton.setToolTipText("����");
		helpButton.setBorderPainted(false);
		aboutButton = new JButton(new ImageIcon(getClass().getResource(
				"/images/about.png")));
		aboutButton.setToolTipText("����");
		aboutButton.setBorderPainted(false);

		TOOLBAR.setFloatable(false);
		TOOLBAR.add(newButton);
		TOOLBAR.add(openButton);
		TOOLBAR.add(saveButton);
		TOOLBAR.addSeparator();
		TOOLBAR.addSeparator();
		TOOLBAR.addSeparator();
		TOOLBAR.addSeparator();
		TOOLBAR.add(lexButton);
		TOOLBAR.add(parseButton);
		TOOLBAR.add(runButton);
		TOOLBAR.addSeparator();
		TOOLBAR.addSeparator();
		TOOLBAR.addSeparator();
		TOOLBAR.addSeparator();
		TOOLBAR.add(undoButton);
		TOOLBAR.add(redoButton);
		TOOLBAR.add(copyButton);
		TOOLBAR.add(cutButton);
		TOOLBAR.add(pasteButton);
		TOOLBAR.add(searchButton);
		TOOLBAR.addSeparator();
		TOOLBAR.addSeparator();
		TOOLBAR.addSeparator();
		TOOLBAR.addSeparator();
		TOOLBAR.add(fontButton);
		TOOLBAR.add(helpButton);
		TOOLBAR.add(aboutButton);
		add(TOOLBAR);
		TOOLBAR.setBounds(0, 0, 1240, 50);
		TOOLBAR.setPreferredSize(getPreferredSize());

		// �ļ�����ʹ򿪶Ի���
		filedialog_save = new FileDialog(this, "�����ļ�", FileDialog.SAVE);
		filedialog_save.setVisible(false);
		filedialog_load = new FileDialog(this, "���ļ�", FileDialog.LOAD);
		filedialog_load.setVisible(false);
		filedialog_save.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				filedialog_save.setVisible(false);
			}
		});
		filedialog_load.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				filedialog_load.setVisible(false);
			}
		});

		// ��Դ������(�ļ�Ŀ¼�����)
		JPanel fileScanPanel = new JPanel(new BorderLayout());
		JLabel fileLabel = new JLabel("��Դ������");
		JPanel fileLabelPanel = new JPanel(new BorderLayout());
		fileLabel.setFont(LABELFONT);
		fileLabelPanel.add(fileLabel, BorderLayout.WEST);
		fileLabelPanel.setBackground(Color.LIGHT_GRAY);
		fileScanPanel.add(fileLabelPanel, BorderLayout.NORTH);
		JScrollPane scrollPane1 = new JScrollPane(FILETREE);
		scrollPane1.getVerticalScrollBar().setUI(new DemoScrollBarUI());
		fileScanPanel.add(scrollPane1, BorderLayout.CENTER);
		add(fileScanPanel);
		fileScanPanel.setBounds(0, TOOLBAR.getHeight(), 195, 768
				- TOOLBAR.getHeight() - STATUSBAR.getHeight() - 98);

		// CMM�ı��༭��
		editTabbedPane = new JCloseableTabbedPane();
		editTabbedPane.setFont(treeFont);

		final StyleEditor editor = new StyleEditor();
		editor.setFont(font);
		JScrollPane scrollPane = new JScrollPane(editor);
		scrollPane.getVerticalScrollBar().setUI(new DemoScrollBarUI());
		TextLineNumber tln = new TextLineNumber(editor);
		scrollPane.setRowHeaderView(tln);

		editor.addMouseListener(new DefaultMouseAdapter());
		editor.addCaretListener(new StatusListener());
		editor.getDocument().addUndoableEditListener(undoHandler);
		// ���Ĭ�Ͻ���
		addWindowListener(new WindowAdapter() {
			public void windowOpened(WindowEvent evt) {
				editor.requestFocus();
				STATUSBAR.setStatus(0, "��ǰ�к�: " + 1 + ", ��ǰ�к�: " + 1);
			}
		});
		map.put(scrollPane, editor);
		editTabbedPane.add(scrollPane, "CMMTest" + ".cmm");
		JPanel editPanel = new JPanel(null);
		editPanel.setBackground(getBackground());
		editPanel.setForeground(new Color(238, 238, 238));
		JLabel editLabel = new JLabel("|CMM�����ı��༭��");
		JPanel editLabelPanel = new JPanel(new BorderLayout());
		editLabel.setFont(LABELFONT);
		editLabelPanel.add(editLabel, BorderLayout.WEST);
		editLabelPanel.setBackground(Color.LIGHT_GRAY);

		// �������ʹ����б���
		consoleArea.setEditable(false);
		problemArea.setRows(6);
		problemArea.setEditable(false);
		consoleArea.setFont(font);
		problemArea.setFont(conAndErrFont);
		proAndConPanel = new JTabbedPane();
		proAndConPanel.setFont(treeFont);
		proAndConPanel.add(new JScrollPane(consoleArea), "����̨");
		proAndConPanel.add(new JScrollPane(problemArea), "�����б�");

		editPanel.add(editLabelPanel);
		editPanel.add(editTabbedPane);
		editPanel.add(proAndConPanel);
		editLabelPanel.setBounds(0, 0, 815, 15);
		editTabbedPane.setBounds(0, 15, 815, 462);
		proAndConPanel.setBounds(0, 475, 815, 160);
		add(editPanel);
		editPanel.setBounds(fileScanPanel.getWidth(), TOOLBAR.getHeight(), 815,
				768 - TOOLBAR.getHeight() - STATUSBAR.getHeight() - 98);

		// �ʷ����������ʾ��
		JScrollPane lexerPanel = new JScrollPane(null);
		JScrollPane parserPanel = new JScrollPane(null);
		tabbedPanel = new JTabbedPane(JTabbedPane.TOP,
				JTabbedPane.SCROLL_TAB_LAYOUT);
		tabbedPanel.setFont(treeFont);
		tabbedPanel.add(lexerPanel, "�ʷ�����");
		tabbedPanel.add(parserPanel, "�﷨����");
		JPanel resultPanel = new JPanel(new BorderLayout());
		JLabel resultLabel = new JLabel("|���������ʾ��");
		JPanel resultLabelPanel = new JPanel(new BorderLayout());
		resultLabel.setFont(LABELFONT);
		resultLabelPanel.add(resultLabel, BorderLayout.WEST);
		resultLabelPanel.setBackground(Color.LIGHT_GRAY);
		resultPanel.add(resultLabelPanel, BorderLayout.NORTH);
		resultPanel.add(tabbedPanel, BorderLayout.CENTER);
		add(resultPanel);
		resultPanel.setBounds(fileScanPanel.getWidth() + editPanel.getWidth(),
				TOOLBAR.getHeight(), 1200 - fileScanPanel.getWidth()
						- editPanel.getWidth() + 38, 768 - TOOLBAR.getHeight()
						- STATUSBAR.getHeight() - 98);

		// ����״̬��
		STATUSBAR.addStatusCell(6666);
		add(STATUSBAR);
		STATUSBAR.setBounds(0, TOOLBAR.getHeight() + editPanel.getHeight(),
				1240, 20);

		// ΪFILETREE���˫����������ʹ����˫��һ���ļ�ʱ�򿪸��ļ�
		FILETREE.setFont(treeFont);
		FILETREE.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					String str = "", fileName = "";
					StringBuilder text = new StringBuilder();
					File file = FILETREE.getSelectFile();
					fileName = file.getName();
					if (file.isFile()) {
						if (fileName.endsWith(".cmm")
								|| fileName.endsWith(".CMM")
								|| fileName.endsWith(".txt")
								|| fileName.endsWith(".TXT")
								|| fileName.endsWith(".java")) {
							try {
								FileReader file_reader = new FileReader(file);
								BufferedReader in = new BufferedReader(
										file_reader);
								while ((str = in.readLine()) != null)
									text.append(str + '\n');
								in.close();
								file_reader.close();
							} catch (IOException e2) {
							}
							create(fileName);
							editTabbedPane.setTitleAt(editTabbedPane
									.getComponentCount() - 1, fileName);
							map.get(editTabbedPane.getSelectedComponent())
									.setText(text.toString());
						}
					}
					setSize(getWidth(), getHeight());
				}
			}
		});

		doc = consoleArea.getStyledDocument();
		consoleArea.addKeyListener(new KeyAdapter() {

			// ����ĳ��
			public void keyPressed(KeyEvent e) {
				// ��õ�ǰ���к���λ��
				getCurrenRowAndCol();
				if (rowNum > presentMaxRow) {
					presentMaxRow = rowNum;
				}
				if (rowNum < presentMaxRow) {
					consoleArea.setCaretPosition(doc.getLength());
					getCurrenRowAndCol();
				}
				if (e.getKeyChar() == KeyEvent.VK_ENTER) {
					consoleArea.setCaretPosition(doc.getLength());
				}
				if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
					if (columnNum == 1) {
						setControlArea(Color.BLACK, false);
					}
				}
			}

			// �ͷ�ĳ��
			public void keyReleased(KeyEvent e) {
				// ��õ�ǰ���к���λ��
				getCurrenRowAndCol();
				if (rowNum > presentMaxRow) {
					presentMaxRow = rowNum;
				}
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					// ��ù�����0��0�е�λ��
					int pos = consoleArea.getCaretPosition();
					index[0] = index[1];
					index[1] = pos;
					try {
						userInput = doc.getText(index[0], index[1] - 1
								- index[0]);
						semanticAnalysis.setUserInput(userInput);
					} catch (BadLocationException e1) {
						e1.printStackTrace();
					}
					setControlArea(Color.BLACK, false);
				}
				if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
					if (rowNum <= presentMaxRow) {
						consoleArea.setEditable(true);
					}
				}
			}
		});
		// Ϊ�˵�������¼�������
		newItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent paramActionEvent) {
				create(null);
			}
		});
		openItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				open();
			}
		});
		saveItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				save();
			}
		});
		exitItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		undoItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				undo();
			}
		});
		redoItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				redo();
			}
		});
		copyItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				copy();
			}
		});
		cutItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				cut();
			}
		});
		pasteItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				paste();
			}
		});
		allItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				selectAll();
			}
		});
		searchItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				search();
			}
		});
		deleteItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				delete();
			}
		});
		lexItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				lex();
			}
		});
		parseItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				parse();
			}
		});
		runItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				run();
			}
		});
		fontItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent paramActionEvent) {
				setFont();
			}
		});
		helpItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JOptionPane.showMessageDialog(new JOptionPane(),
						"����һ���򵥵�CMM���Ա����������Զ�CMM\n�����ļ����б༭��"
								+ "�ʷ��������﷨������������\n���б��롢���к������������ "
								+ "ͬʱ��ʵ����\n�Գ�����г�����Ĺ���.", "����",
						JOptionPane.INFORMATION_MESSAGE);
			}
		});
		aboutItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(new JOptionPane(),
						"����",
						"����CMM������", JOptionPane.INFORMATION_MESSAGE);
			}
		});

		// Ϊ�Ҽ��˵�����¼�������
		item1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				copy();
			}
		});
		item2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				cut();
			}
		});
		item3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				paste();
			}
		});
		item4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				selectAll();
			}
		});

		// Ϊ��������ť����¼�������
		newButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent paramActionEvent) {
				create(null);
			}
		});
		openButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				open();
			}
		});
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				save();
			}
		});
		runButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				run();
			}
		});
		lexButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				lex();
			}
		});
		parseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				parse();
			}
		});
		undoButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				undo();
			}
		});
		redoButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				redo();
			}
		});
		copyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				copy();
			}
		});
		cutButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				cut();
			}
		});
		pasteButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				paste();
			}
		});
		searchButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				search();
			}
		});
		fontButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				setFont();
			}
		});
		helpButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JOptionPane.showMessageDialog(new JOptionPane(),
						"����һ���򵥵�CMM���Ա����������Զ�CMM\n�����ļ����б༭��"
								+ "�ʷ��������﷨������������\n���б��롢���к������������ "
								+ "ͬʱ��ʵ����\n�Գ�����г�����Ĺ���.", "����",
						JOptionPane.INFORMATION_MESSAGE);
			}
		});
		aboutButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(new JOptionPane(),
						"����",
						"����CMM������", JOptionPane.INFORMATION_MESSAGE);
			}
		});
	}

	// �ڲ��ࣺ��������Ҽ�
	class DefaultMouseAdapter extends MouseAdapter {
		public void mouseReleased(MouseEvent e) {
			if (SwingUtilities.isRightMouseButton(e)) {
				popupMenu.show(e.getComponent(), e.getX(), e.getY());
			}
		}
	}

	// �ڲ��ࣺUndo����
	class UndoHandler implements UndoableEditListener {
		public void undoableEditHappened(UndoableEditEvent e) {
			undo.addEdit(e.getEdit());
		}
	}

	// �ڲ��ࣺ����״̬������ʾ
	class StatusListener implements CaretListener {
		public void caretUpdate(CaretEvent e) {
			StyleEditor temp = map.get(editTabbedPane.getSelectedComponent());
			try {
				int row = temp.getLineOfOffset(e.getDot());
				int column = e.getDot() - temp.getLineStartOffset(row);
				STATUSBAR.setStatus(0, "��ǰ�к�: " + (row + 1) + ", ��ǰ�к�: "
						+ (column + 1));
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
	}

	// �ʷ��������Գ���Ĵʷ����з���������������
	public void lex() {
		StyleEditor textArea = map.get(editTabbedPane.getSelectedComponent());
		String text = textArea.getText();

		if (text.equals("")) {
			JOptionPane.showMessageDialog(new JPanel(), "��ȷ������CMM����Ϊ�գ�");
		} else {
			TreeNode root = lexer.execute(text);
			DefaultTreeModel model = new DefaultTreeModel(root);
			JTree lexerTree = new JTree(model);
			// ���ø�JTreeʹ���Զ���Ľڵ������
			lexerTree.setCellRenderer(new JTreeRenderer());
			// �����Ƿ���ʾ���ڵ�ġ�չ��/�۵���ͼ��,Ĭ����false
			lexerTree.setShowsRootHandles(true);
			// ���ýڵ��Ƿ�ɼ�,Ĭ����true
			lexerTree.setRootVisible(true);
			// ��������
			lexerTree.setFont(treeFont);

			tabbedPanel.setComponentAt(0, new JScrollPane(lexerTree));
			tabbedPanel.setSelectedIndex(0);
			problemArea.setText("**********�ʷ��������**********\n");
			problemArea.append(lexer.getErrorInfo());
			problemArea.append("�ó����й���" + lexer.getErrorNum() + "���ʷ�����\n");
			proAndConPanel.setSelectedIndex(1);
		}
	}

	// �﷨�������Գ�����﷨���з���������ʾ�﷨��
	public TreeNode parse() {
		lex();
		if (lexer.getErrorNum() != 0) {
			JOptionPane.showMessageDialog(new JPanel(),
					"�ʷ��������ִ��������޸ĳ����ٽ����﷨������", "�﷨����",
					JOptionPane.ERROR_MESSAGE);
			return null;
		}
		parser = new CMMParser(lexer.getTokens());
		parser.setIndex(0);
		parser.setErrorInfo("");
		parser.setErrorNum(0);
		TreeNode root = parser.execute();
		DefaultTreeModel model = new DefaultTreeModel(root);
		JTree parserTree = new JTree(model);
		// ���ø�JTreeʹ���Զ���Ľڵ������
		parserTree.setCellRenderer(new JTreeRenderer());

		// �����Ƿ���ʾ���ڵ�ġ�չ��/�۵���ͼ��,Ĭ����false
		parserTree.setShowsRootHandles(true);
		// ���ýڵ��Ƿ�ɼ�,Ĭ����true
		parserTree.setRootVisible(true);
		// ��������
		parserTree.setFont(treeFont);
		problemArea.append("\n");
		problemArea.append("**********�﷨�������**********\n");
		if (parser.getErrorNum() != 0) {
			problemArea.append(parser.getErrorInfo());
			problemArea.append("�ó����й���" + parser.getErrorNum() + "���﷨����\n");
			JOptionPane.showMessageDialog(new JPanel(), "��������﷨����ʱ���ִ������޸ģ�",
					"�﷨����", JOptionPane.ERROR_MESSAGE);
		} else {
			problemArea.append("�ó����й���" + parser.getErrorNum() + "���﷨����\n");
		}
		tabbedPanel.setComponentAt(1, new JScrollPane(parserTree));
		tabbedPanel.setSelectedIndex(1);
		proAndConPanel.setSelectedIndex(1);
		return root;
	}

	// ���У�����������CMM������ʾ���н��
	public void run() {
		consoleArea.setText(null);
		columnNum = 0;
		rowNum = 0;
		presentMaxRow = 0;
		index = new int[] { 0, 0 };
		TreeNode node = parse();
		if (lexer.getErrorNum() != 0) {
			return;
		} else if (parser.getErrorNum() != 0 || node == null) {
			return;
		} else {
			semanticAnalysis = new CMMSemanticAnalysis(node);
			semanticAnalysis.start();
		}
	}

	// �½�
	private void create(String filename) {
		if (filename == null) {
			filename = JOptionPane.showInputDialog("�������½��ļ�������.(��׺��Ϊ.cmm)");
			if (filename == null || filename.equals("")) {
				JOptionPane.showMessageDialog(null, "�ļ�������Ϊ��!");
				return;
			}
		}
		filename += ".cmm";
		StyleEditor editor = new StyleEditor();
		editor.setFont(font);
		JScrollPane scrollPane = new JScrollPane(editor);
		TextLineNumber tln = new TextLineNumber(editor);
		scrollPane.setRowHeaderView(tln);

		editor.addMouseListener(new DefaultMouseAdapter());
		editor.addCaretListener(new StatusListener());
		editor.getDocument().addUndoableEditListener(undoHandler);
		map.put(scrollPane, editor);
		editTabbedPane.add(scrollPane, filename);
		editTabbedPane.setSelectedIndex(editTabbedPane.getTabCount() - 1);
	}

	// ��
	private void open() {
		boolean isOpened = false;
		String str = "", fileName = "";
		File file = null;
		StringBuilder text = new StringBuilder();
		filedialog_load.setVisible(true);
		if (filedialog_load.getFile() != null) {
			try {
				file = new File(filedialog_load.getDirectory(), filedialog_load
						.getFile());
				fileName = file.getName();
				FileReader file_reader = new FileReader(file);
				BufferedReader in = new BufferedReader(file_reader);
				while ((str = in.readLine()) != null)
					text.append(str + '\n');
				in.close();
				file_reader.close();
			} catch (IOException e2) {
			}
			for (int i = 0; i < editTabbedPane.getComponentCount(); i++) {
				if (editTabbedPane.getTitleAt(i).equals(fileName)) {
					isOpened = true;
					editTabbedPane.setSelectedIndex(i);
				}
			}
			if (!isOpened) {
				create(fileName);
				editTabbedPane.setTitleAt(
						editTabbedPane.getComponentCount() - 1, fileName);
				map.get(editTabbedPane.getSelectedComponent()).setText(
						text.toString());
			}

		}
	}

	// ����
	private void save() {
		StyleEditor temp = map.get(editTabbedPane.getSelectedComponent());
		if (temp.getText() != null) {
			filedialog_save.setVisible(true);
			if (filedialog_save.getFile() != null) {
				try {
					File file = new File(filedialog_save.getDirectory(),
							filedialog_save.getFile());
					FileWriter fw = new FileWriter(file);
					fw.write(map.get(editTabbedPane.getSelectedComponent())
							.getText());
					fw.close();
				} catch (IOException e2) {
				}
			}
		}
	}

	// ����
	private void undo() {
		if (undo.canUndo()) {
			try {
				undo.undo();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// ����
	private void redo() {
		if (undo.canRedo()) {
			try {
				undo.redo();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// ����
	private void copy() {
		StyleEditor temp = map.get(editTabbedPane.getSelectedComponent());
		temp.copy();
	}

	// ����
	private void cut() {
		StyleEditor temp = map.get(editTabbedPane.getSelectedComponent());
		temp.cut();
	}

	// ճ��
	private void paste() {
		StyleEditor temp = map.get(editTabbedPane.getSelectedComponent());
		temp.paste();
	}

	// ����
	private void search() {
		StyleEditor temp = map.get(editTabbedPane.getSelectedComponent());
		if (text == null)
			text = temp.getText();
		if (findStr == null)
			findStr = JOptionPane.showInputDialog(this, "������Ҫ�ҵ��ַ���!");
		if (findStr != null) {
			position = text.indexOf(findStr);
			if (text.equals("")) {
				JOptionPane.showMessageDialog(this, "û����Ҫ���ҵ��ַ�����");
				findStr = null;
			} else {
				if (position != -1) {
					temp.select(position + findStr.length() * time, position
							+ findStr.length() * (time + 1));
					temp.setSelectedTextColor(Color.RED);
					text = new String(text.substring(position
							+ findStr.length()));
					time += 1;
				} else {
					JOptionPane.showMessageDialog(this, "û����Ҫ���ҵ��ַ�����");
					time = 0;
					text = null;
					findStr = null;
				}
			}
		}
	}

	// ȫѡ
	private void selectAll() {
		StyleEditor temp = map.get(editTabbedPane.getSelectedComponent());
		temp.selectAll();
	}

	// ɾ��
	private void delete() {
		StyleEditor temp = map.get(editTabbedPane.getSelectedComponent());
		temp.replaceSelection("");
	}

	// ��������
	private void setFont() {
		font = JFontDialog
				.showDialog(getContentPane(), "��������", true, getFont());
		for (int i = 0; i < editTabbedPane.getComponentCount(); i++)
			map.get(editTabbedPane.getComponent(i)).setFont(font);
	}

	private void getCurrenRowAndCol() {
		int row = 0;
		int col = 0;
		// ��ù�����0��0�е�λ��
		int pos = consoleArea.getCaretPosition();
		Element root = consoleArea.getDocument().getDefaultRootElement();
		int index = root.getElementIndex(doc.getParagraphElement(pos)
				.getStartOffset());
		// ��!!!
		try {
			col = pos
					- doc.getText(0, doc.getLength()).substring(0, pos)
							.lastIndexOf("\n");
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		// ��!!!
		try {
			// �������Ǵ�0�����,����+1
			row = Integer.parseInt(String.valueOf(index + 1));
		} catch (Exception e) {
			e.printStackTrace();
		}
		rowNum = row;
		columnNum = col;
		presentMaxRow = root.getElementIndex(doc.getParagraphElement(
				doc.getLength()).getStartOffset()) + 1;
	}

	// �ı�controlArea����ɫ��༭����
	public static void setControlArea(Color c, boolean edit) {
		proAndConPanel.setSelectedIndex(0);
		consoleArea.setFocusable(true);
		consoleArea.setForeground(c);
		consoleArea.setEditable(edit);
	}

	// ������
	class DemoScrollBarUI extends BasicScrollBarUI {
		@Override
		protected void configureScrollBarColors() {
			// ����
			// thumbColor = Color.GRAY;
			// thumbHighlightColor = Color.BLUE;
			// thumbDarkShadowColor = Color.BLACK;
			// thumbLightShadowColor = Color.YELLOW;

			// ����
			trackColor = Color.black;
			setThumbBounds(0, 0, 10, 10);
			// trackHighlightColor = Color.GREEN;
		}

		/**
		 * ���ù������Ŀ��
		 */
		@Override
		public Dimension getPreferredSize(JComponent c) {
			// TODO Auto-generated method stub
			c.setPreferredSize(new Dimension(10, 10));
			return super.getPreferredSize(c);
		}

		// �ػ滬��Ļ������򱳾�
		public void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
			Graphics2D g2 = (Graphics2D) g;
			GradientPaint gp = null;
			//�жϹ������Ǵ�ֱ�� ����ˮƽ��
			if (this.scrollbar.getOrientation() == JScrollBar.VERTICAL) {
				//���û���
				gp = new GradientPaint(0, 0, new Color(255, 255, 255),
						trackBounds.width, 0, new Color(255, 255, 255));
			}
			if (this.scrollbar.getOrientation() == JScrollBar.HORIZONTAL) {
				gp = new GradientPaint(0, 0, new Color(255, 255, 255),
						trackBounds.height, 0, new Color(255, 255, 255));
			}

			g2.setPaint(gp);
			//���Track
			g2.fillRect(trackBounds.x, trackBounds.y, trackBounds.width,
					trackBounds.height);
			//����Track�ı߿�
		    //g2.setColor(new Color(175, 155, 95));
		    //g2.drawRect(trackBounds.x, trackBounds.y, trackBounds.width - 1,trackBounds.height - 1);

			if (trackHighlight == BasicScrollBarUI.DECREASE_HIGHLIGHT)
				this.paintDecreaseHighlight(g);
			if (trackHighlight == BasicScrollBarUI.INCREASE_HIGHLIGHT)
				this.paintIncreaseHighlight(g);
		}

		@Override
		protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
			// �ѻ�������x��y�����궨��Ϊ����ϵ��ԭ��
			// ���һ��һ��Ҫ���ϰ�����Ȼ�϶���ʧЧ��
			g.translate(thumbBounds.x, thumbBounds.y);
			// ���ð�����ɫ
			g.setColor(new Color(152, 152, 152));
			// ��һ��Բ�Ǿ���
			// ������ǰ�ĸ������Ͳ��ི�ˣ�����Ϳ��
			// ������������Ҫע��һ�£����������ƽ����Բ�ǻ���
			// g.drawRoundRect(0, 0, 5, thumbBounds.height - 1, 5, 5);
			// �������
			Graphics2D g2 = (Graphics2D) g;
			RenderingHints rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			g2.addRenderingHints(rh);
			// ��͸��
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
					0.5f));
			// ���������ɫ�����������˽��䣬��������
			// g2.setPaint(new GradientPaint(c.getWidth() / 2, 1, Color.GRAY,
			// c.getWidth() / 2, c.getHeight(), Color.GRAY));
			// ���Բ�Ǿ���
			g2.fillRoundRect(0, 0, 10, thumbBounds.height - 1, 5, 5);
		}

		/**
		 * �����������Ϸ��İ�ť
		 */
		@Override
		protected JButton createIncreaseButton(int orientation) {
			JButton button = new JButton();
			button.setBorderPainted(false);
			button.setContentAreaFilled(false);
			button.setBorder(null);
			return button;
		}

		/**
		 * �����������·��İ�ť
		 */
		@Override
		protected JButton createDecreaseButton(int orientation) {
			JButton button = new JButton();
			button.setBorderPainted(false);
			button.setContentAreaFilled(false);
			button.setFocusable(false);
			button.setBorder(null);
			return button;
		}



	}

	// ��ť
	class MyImageIcon extends ImageIcon {
		@Override
		public int getIconHeight() {
			return super.getIconHeight();
		}

		@Override
		public int getIconWidth() {
			return super.getIconWidth();
		}
	}
	// ������
	public static void main(String[] args) {
		CompilerFrame frame = new CompilerFrame("CMM������");
		frame.setBounds(60, 0, 1240, 750);
		frame.setResizable(false);//���ܸı��С
		frame.setVisible(true);
	}

}
