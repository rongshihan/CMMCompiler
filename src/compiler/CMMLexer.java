package compiler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import structure.ConstVar;
import structure.Token;
import structure.TreeNode;

/**
 * CMM�ʷ�������
 */

public class CMMLexer {
	// ע�͵ı�־
	private boolean isNotation = false;
	// �������
	private int errorNum = 0;
	// ������Ϣ
	private String errorInfo = "";
	// ������õ���tokens���ϣ����������﷨���������
	private ArrayList<Token> tokens = new ArrayList<Token>();
	// ������õ�������tokens���ϣ�����ע�͡��ո��
	private ArrayList<Token> displayTokens = new ArrayList<Token>();
	// ��ȡCMM�ļ��ı�
	private BufferedReader reader;

	public boolean isNotation() {
		return isNotation;
	}

	public void setNotation(boolean isNotation) {
		this.isNotation = isNotation;
	}

	public int getErrorNum() {
		return errorNum;
	}

	public void setErrorNum(int errorNum) {
		this.errorNum = errorNum;
	}

	public String getErrorInfo() {
		return errorInfo;
	}

	public void setErrorInfo(String errorInfo) {
		this.errorInfo = errorInfo;
	}

	public ArrayList<Token> getTokens() {
		return tokens;
	}

	public void setTokens(ArrayList<Token> tokens) {
		this.tokens = tokens;
	}

	public ArrayList<Token> getDisplayTokens() {
		return displayTokens;
	}

	public void setDisplayTokens(ArrayList<Token> displayTokens) {
		this.displayTokens = displayTokens;
	}

	/**
	 * ʶ����ĸ
	 * 
	 * @param c
	 *            Ҫʶ����ַ�
	 * @return
	 */
	private static boolean isLetter(char c) {
		if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_')
			return true;
		return false;
	}

	/**
	 * ʶ������
	 * 
	 * @param c
	 *            Ҫʶ����ַ�
	 * @return
	 */
	private static boolean isDigit(char c) {
		if (c >= '0' && c <= '9')
			return true;
		return false;
	}

	/**
	 * ʶ����ȷ���������ų����������
	 * 
	 * @param input
	 *            Ҫʶ����ַ���
	 * @return ����ֵ
	 */
	private static boolean matchInteger(String input) {
		if (input.matches("^-?\\d+$") && !input.matches("^-?0{1,}\\d+$"))
			return true;
		else
			return false;
	}

	/**
	 * ʶ����ȷ�ĸ��������ų�00.000�����
	 * 
	 * @param input
	 *            Ҫʶ����ַ���
	 * @return ����ֵ
	 */
	private static boolean matchReal(String input) {
		if (input.matches("^(-?\\d+)(\\.\\d+)+$")
				&& !input.matches("^(-?0{2,}+)(\\.\\d+)+$"))
			return true;
		else
			return false;
	}

	/**
	 * ʶ����ȷ�ı�ʶ��������ĸ�����֡��»�����ɣ���������ĸ��ͷ���������»��߽�β
	 * 
	 * @param input
	 *            Ҫʶ����ַ���
	 * @return ����ֵ
	 */
	private static boolean matchID(String input) {
		if (input.matches("^\\w+$") && !input.endsWith("_")
				&& input.substring(0, 1).matches("[A-Za-z]"))
			return true;
		else
			return false;
	}

	/**
	 * ʶ������
	 * 
	 * @param str
	 *            Ҫ�������ַ���
	 * @return ����ֵ
	 */
	private static boolean isKey(String str) {
		if (str.equals(ConstVar.IF) || str.equals(ConstVar.ELSE)
				|| str.equals(ConstVar.WHILE) || str.equals(ConstVar.READ)
				|| str.equals(ConstVar.WRITE) || str.equals(ConstVar.INT)
				|| str.equals(ConstVar.REAL) || str.equals(ConstVar.BOOL)
				|| str.equals(ConstVar.STRING) || str.equals(ConstVar.TRUE)
				|| str.equals(ConstVar.FALSE) || str.equals(ConstVar.FOR))
			return true;
		return false;
	}

	private static int find(int begin, String str) {
		if (begin >= str.length())
			return str.length();
		for (int i = begin; i < str.length(); i++) {
			char c = str.charAt(i);
			if (c == '\n' || c == ',' || c == ' ' || c == '\t' || c == '{'
					|| c == '}' || c == '(' || c == ')' || c == ';' || c == '='
					|| c == '+' || c == '-' || c == '*' || c == '/' || c == '['
					|| c == ']' || c == '<' || c == '>')
				return i - 1;
		}
		return str.length();
	}

	/**
	 * ����һ��CMM���򣬲����ط���һ�еõ���TreeNode
	 * 
	 * @param cmmText
	 *            ��ǰ���ַ���
	 * @param lineNum
	 *            ��ǰ�к�
	 * @return �������ɵ�TreeNode
	 */
	private TreeNode executeLine(String cmmText, int lineNum) {
		// ������ǰ�и����
		String content = "��" + lineNum + "�У� " + cmmText;
		TreeNode node = new TreeNode(content);
		// �ʷ�����ÿ�н����ı�־
		cmmText += "\n";
		int length = cmmText.length();
		// switch״ֵ̬
		int state = 0;
		// ��¼token��ʼλ��
		int begin = 0;
		// ��¼token����λ��
		int end = 0;
		// �����ȡ��ǰ���ַ������з�������������ж�����ǰ�࿴kλ
		for (int i = 0; i < length; i++) {
			char ch = cmmText.charAt(i);
			if (!isNotation) {
				if (ch == '(' || ch == ')' || ch == ';' || ch == '{'
						|| ch == '}' || ch == '[' || ch == ']' || ch == ','
						|| ch == '+' || ch == '-' || ch == '*' || ch == '/'
						|| ch == '=' || ch == '<' || ch == '>' || ch == '"'
						|| isLetter(ch) || isDigit(ch)
						|| String.valueOf(ch).equals(" ")
						|| String.valueOf(ch).equals("\n")
						|| String.valueOf(ch).equals("\r")
						|| String.valueOf(ch).equals("\t")) {
					switch (state) {
					case 0:
						// �ָ���ֱ�Ӵ�ӡ
						if (ch == '(' || ch == ')' || ch == ';' || ch == '{'
								|| ch == '}' || ch == '[' || ch == ']'
								|| ch == ',') {
							state = 0;
							node.add(new TreeNode("�ָ��� �� " + ch));
							tokens.add(new Token(lineNum, i + 1, "�ָ���", String
									.valueOf(ch)));
							displayTokens.add(new Token(lineNum, i + 1, "�ָ���",
									String.valueOf(ch)));
						}
						// �Ӻ�+
						else if (ch == '+')
							state = 1;
						// ����-
						else if (ch == '-')
							state = 2;
						// �˺�*
						else if (ch == '*')
							state = 3;
						// ����/
						else if (ch == '/')
							state = 4;
						// ��ֵ����==���ߵȺ�=
						else if (ch == '=')
							state = 5;
						// С�ڷ���<���߲�����<>
						else if (ch == '<')
							state = 6;
						// ����>
						else if (ch == '>')
							state = 9;
						// �ؼ��ֻ��߱�ʶ��
						else if (isLetter(ch)) {
							state = 7;
							begin = i;
						}
						// �������߸�����
						else if (isDigit(ch)) {
							begin = i;
							state = 8;
						}
						// ˫����"
						else if (String.valueOf(ch).equals(ConstVar.DQ)) {
							begin = i + 1;
							state = 10;
							node.add(new TreeNode("�ָ��� �� " + ch));
							tokens.add(new Token(lineNum, begin, "�ָ���",
									ConstVar.DQ));
							displayTokens.add(new Token(lineNum, begin, "�ָ���",
									ConstVar.DQ));
						}
						// �հ׷�
						else if (String.valueOf(ch).equals(" ")) {
							state = 0;
							displayTokens.add(new Token(lineNum, i + 1, "�հ׷�",
									" "));
						}
						// ���з�
						else if (String.valueOf(ch).equals("\n")) {
							state = 0;
							displayTokens.add(new Token(lineNum, i + 1, "���з�",
									"\n"));
						}
						// �س���
						else if (String.valueOf(ch).equals("\r")) {
							state = 0;
							displayTokens.add(new Token(lineNum, i + 1, "�س���",
									"\r"));
						}
						// �Ʊ��
						else if (String.valueOf(ch).equals("\t")) {
							state = 0;
							displayTokens.add(new Token(lineNum, i + 1, "�Ʊ��",
									"\t"));
						}
						break;
					case 1:
						node.add(new TreeNode("����� �� " + ConstVar.PLUS));
						tokens.add(new Token(lineNum, i, "�����", ConstVar.PLUS));
						displayTokens.add(new Token(lineNum, i, "�����",
								ConstVar.PLUS));
						i--;
						state = 0;
						break;
					case 2:
						String temp = tokens.get(tokens.size() - 1).getKind();
						String c = tokens.get(tokens.size() - 1).getContent();
						if (temp.equals("����") || temp.equals("��ʶ��")
								|| temp.equals("ʵ��") || c.equals(")")
								|| c.equals("]")) {
							node.add(new TreeNode("����� �� " + ConstVar.MINUS));
							tokens.add(new Token(lineNum, i, "�����",
									ConstVar.MINUS));
							displayTokens.add(new Token(lineNum, i, "�����",
									ConstVar.MINUS));
							i--;
							state = 0;
						} else if (String.valueOf(ch).equals("\n")) {
							displayTokens.add(new Token(lineNum, i - 1, "����",
									ConstVar.MINUS));
						} else {
							begin = i - 1;
							state = 8;
						}
						break;
					case 3:
						if (ch == '/') {
							errorNum++;
							errorInfo += "    ERROR:�� " + lineNum + " ��,�� " + i
									+ " �У�" + "�����\"" + ConstVar.TIMES
									+ "\"ʹ�ô���  \n";
							node.add(new TreeNode(ConstVar.ERROR + "�����\""
									+ ConstVar.TIMES + "\"ʹ�ô���"));
							displayTokens.add(new Token(lineNum, i, "����",
									cmmText.substring(i - 1, i + 1)));
						} else {
							node.add(new TreeNode("����� �� " + ConstVar.TIMES));
							tokens.add(new Token(lineNum, i, "�����",
									ConstVar.TIMES));
							displayTokens.add(new Token(lineNum, i, "�����",
									ConstVar.TIMES));
							i--;
						}
						state = 0;
						break;
					case 4:
						if (ch == '/') {
							node.add(new TreeNode("����ע�� //"));
							displayTokens.add(new Token(lineNum, i, "����ע�ͷ���",
									"//"));
							begin = i + 1;
							displayTokens.add(new Token(lineNum, i, "ע��",
									cmmText.substring(begin, length - 1)));
							i = length - 2;
							state = 0;
						} else if (ch == '*') {
							node.add(new TreeNode("����ע�� /*"));
							displayTokens.add(new Token(lineNum, i, "����ע�Ϳ�ʼ����",
									"/*"));
							begin = i + 1;
							isNotation = true;
						} else {
							node.add(new TreeNode("����� �� " + ConstVar.DIVIDE));
							tokens.add(new Token(lineNum, i, "�����",
									ConstVar.DIVIDE));
							displayTokens.add(new Token(lineNum, i, "�����",
									ConstVar.DIVIDE));
							i--;
							state = 0;
						}
						break;
					case 5:
						if (ch == '=') {
							node.add(new TreeNode("����� �� " + ConstVar.EQUAL));
							tokens.add(new Token(lineNum, i, "�����",
									ConstVar.EQUAL));
							displayTokens.add(new Token(lineNum, i, "�����",
									ConstVar.EQUAL));
							state = 0;
						} else {
							state = 0;
							node.add(new TreeNode("����� �� " + ConstVar.ASSIGN));
							tokens.add(new Token(lineNum, i, "�����",
									ConstVar.ASSIGN));
							displayTokens.add(new Token(lineNum, i, "�����",
									ConstVar.ASSIGN));
							i--;
						}
						break;
					case 6:
						if (ch == '>') {
							node.add(new TreeNode("����� �� " + ConstVar.NEQUAL));
							tokens.add(new Token(lineNum, i, "�����",
									ConstVar.NEQUAL));
							displayTokens.add(new Token(lineNum, i, "�����",
									ConstVar.NEQUAL));
							state = 0;
						} else {
							state = 0;
							node.add(new TreeNode("����� �� " + ConstVar.LT));
							tokens
									.add(new Token(lineNum, i, "�����",
											ConstVar.LT));
							displayTokens.add(new Token(lineNum, i, "�����",
									ConstVar.LT));
							i--;
						}
						break;
					case 7:
						if (isLetter(ch) || isDigit(ch)) {
							state = 7;
						} else {
							end = i;
							String id = cmmText.substring(begin, end);
							if (isKey(id)) {
								node.add(new TreeNode("�ؼ��� �� " + id));
								tokens.add(new Token(lineNum, begin + 1, "�ؼ���",
										id));
								displayTokens.add(new Token(lineNum, begin + 1,
										"�ؼ���", id));
							} else if (matchID(id)) {
								node.add(new TreeNode("��ʶ�� �� " + id));
								tokens.add(new Token(lineNum, begin + 1, "��ʶ��",
										id));
								displayTokens.add(new Token(lineNum, begin + 1,
										"��ʶ��", id));
							} else {
								errorNum++;
								errorInfo += "    ERROR:�� " + lineNum + " ��,�� "
										+ (begin + 1) + " �У�" + id + "�ǷǷ���ʶ��\n";
								node.add(new TreeNode(ConstVar.ERROR + id
										+ "�ǷǷ���ʶ��"));
								displayTokens.add(new Token(lineNum, begin + 1,
										"����", id));
							}
							i--;
							state = 0;
						}
						break;
					case 8:
						if (isDigit(ch) || String.valueOf(ch).equals(".")) {
							state = 8;
						} else {
							if (isLetter(ch)) {
								errorNum++;
								errorInfo += "    ERROR:�� " + lineNum + " ��,�� "
										+ i + " �У�" + "���ָ�ʽ������߱�־������\n";
								node.add(new TreeNode(ConstVar.ERROR
										+ "���ָ�ʽ������߱�־������"));
								displayTokens.add(new Token(lineNum, i, "����",
										cmmText.substring(begin, find(begin,
												cmmText) + 1)));
								i = find(begin, cmmText);
							} else {
								end = i;
								String id = cmmText.substring(begin, end);
								if (!id.contains(".")) {
									if (matchInteger(id)) {
										node.add(new TreeNode("����    �� " + id));
										tokens.add(new Token(lineNum,
												begin + 1, "����", id));
										displayTokens.add(new Token(lineNum,
												begin + 1, "����", id));
									} else {
										errorNum++;
										errorInfo += "    ERROR:�� " + lineNum
												+ " ��,�� " + (begin + 1) + " �У�"
												+ id + "�ǷǷ�����\n";
										node.add(new TreeNode(ConstVar.ERROR
												+ id + "�ǷǷ�����"));
										displayTokens.add(new Token(lineNum,
												begin + 1, "����", id));
									}
								} else {
									if (matchReal(id)) {
										node.add(new TreeNode("ʵ��    �� " + id));
										tokens.add(new Token(lineNum,
												begin + 1, "ʵ��", id));
										displayTokens.add(new Token(lineNum,
												begin + 1, "ʵ��", id));
									} else {
										errorNum++;
										errorInfo += "    ERROR:�� " + lineNum
												+ " ��,�� " + (begin + 1) + " �У�"
												+ id + "�ǷǷ�ʵ��\n";
										node.add(new TreeNode(ConstVar.ERROR
												+ id + "�ǷǷ�ʵ��"));
										displayTokens.add(new Token(lineNum,
												begin + 1, "����", id));
									}
								}
								i = find(i, cmmText);
							}
							state = 0;
						}
						break;
					case 9:
						node.add(new TreeNode("����� �� " + ConstVar.GT));
						tokens.add(new Token(lineNum, i, "�����", ConstVar.GT));
						displayTokens.add(new Token(lineNum, i, "�����",
								ConstVar.GT));
						i--;
						state = 0;
						break;
					case 10:
						if (ch == '"') {
							end = i;
							String string = cmmText.substring(begin, end);
							node.add(new TreeNode("�ַ��� �� " + string));
							tokens.add(new Token(lineNum, begin + 1, "�ַ���",
									string));
							displayTokens.add(new Token(lineNum, begin + 1,
									"�ַ���", string));
							node.add(new TreeNode("�ָ��� �� " + ConstVar.DQ));
							tokens.add(new Token(lineNum, end + 1, "�ָ���",
									ConstVar.DQ));
							displayTokens.add(new Token(lineNum, end + 1,
									"�ָ���", ConstVar.DQ));
							state = 0;
						} else if (i == length - 1) {
							String string = cmmText.substring(begin);
							errorNum++;
							errorInfo += "    ERROR:�� " + lineNum + " ��,�� "
									+ (begin + 1) + " �У�" + "�ַ��� " + string
									+ " ȱ������  \n";
							node.add(new TreeNode(ConstVar.ERROR + "�ַ��� "
									+ string + " ȱ������  \n"));
							displayTokens.add(new Token(lineNum, i + 1, "����",
									string));
						}
					}
				} else {
					if (ch > 19967 && ch < 40870 || ch == '\\' || ch == '~'
							|| ch == '`' || ch == '|' || ch == '��' || ch == '^'
							|| ch == '?' || ch == '&' || ch == '^' || ch == '%'
							|| ch == '$' || ch == '@' || ch == '!' || ch == '#'
							|| ch == '��' || ch == '��' || ch == '��' || ch == '��'
							|| ch == '��' || ch == '��' || ch == '��' || ch == '��'
							|| ch == '��' || ch == '��' || ch == '��' || ch == '��'
							|| ch == '��' || ch == '��' || ch == '��') {
						errorNum++;
						errorInfo += "    ERROR:�� " + lineNum + " ��,�� "
								+ (i + 1) + " �У�" + "\"" + ch + "\"�ǲ���ʶ�����  \n";
						node.add(new TreeNode(ConstVar.ERROR + "\"" + ch
								+ "\"�ǲ���ʶ�����"));
						if (state == 0)
							displayTokens.add(new Token(lineNum, i + 1, "����",
									String.valueOf(ch)));
					}
				}
			} else {
				if (ch == '*') {
					state = 3;
				} else if (ch == '/' && state == 3) {
					node.add(new TreeNode("����ע�� */"));
					displayTokens.add(new Token(lineNum, begin + 1, "ע��",
							cmmText.substring(begin, i - 1)));
					displayTokens.add(new Token(lineNum, i, "����ע�ͽ�������", "*/"));
					state = 0;
					isNotation = false;
				} else if (i == length - 2) {
					displayTokens.add(new Token(lineNum, begin + 1, "ע��",
							cmmText.substring(begin, length - 1)));
					displayTokens.add(new Token(lineNum, length - 1, "���з�",
							"\n"));
					state = 0;
				} else {
					state = 0;
				}
			}
		}
		return node;
	}

	/**
	 * ����CMM���򣬲����شʷ���������ĸ����
	 * 
	 * @param cmmText
	 *            CMM�����ı�
	 * @return �������ɵ�TreeNode
	 */
	public TreeNode execute(String cmmText) {
		setErrorInfo("");
		setErrorNum(0);
		setTokens(new ArrayList<Token>());
		setDisplayTokens(new ArrayList<Token>());
		setNotation(false);
		StringReader stringReader = new StringReader(cmmText);
		String eachLine = "";
		int lineNum = 1;
		TreeNode root = new TreeNode("PROGRAM");
		reader = new BufferedReader(stringReader);
		while (eachLine != null) {
			try {
				eachLine = reader.readLine();
				if (eachLine != null) {
					if (isNotation() && !eachLine.contains("*/")) {
						eachLine += "\n";
						TreeNode temp = new TreeNode(eachLine);
						temp.add(new TreeNode("����ע��"));
						displayTokens.add(new Token(lineNum, 1, "ע��", eachLine
								.substring(0, eachLine.length() - 1)));
						displayTokens.add(new Token(lineNum,
								eachLine.length() - 1, "���з�", "\n"));
						root.add(temp);
						lineNum++;
						continue;
					} else {
						root.add((executeLine(eachLine, lineNum)));
					}
				}
				lineNum++;
			} catch (IOException e) {
				System.err.println("��ȡ�ı�ʱ�����ˣ�");
			}
		}
		return root;
	}

}