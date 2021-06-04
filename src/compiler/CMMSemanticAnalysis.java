package compiler;

import java.awt.Color;
import java.math.BigDecimal;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import structure.ConstVar;
import structure.SymbolTable;
import structure.SymbolTableElement;
import structure.TreeNode;

/**
 * CMM���������
 */
public class CMMSemanticAnalysis extends Thread {
	/* �������ʱ�ķ��ű� */
	private SymbolTable table = new SymbolTable();
	/* �﷨�����õ��ĳ����﷨�� */
	private TreeNode root;
	/* �������������Ϣ */
	private String errorInfo = "";
	/* �������������� */
	private int errorNum = 0;
	/* ���������ʶ�������� */
	private int level = 0;
	/* �û����� */
	private String userInput;

	public CMMSemanticAnalysis(TreeNode root) {
		this.root = root;
	}

	public void error(String error, int line) {
		errorNum++;
		String s = ConstVar.ERROR + "�� " + line + " �У�" + error + "\n";
		errorInfo += s;
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
	 * �����û�����
	 * 
	 * @param userInput
	 *            ���������
	 */
	public synchronized void setUserInput(String userInput) {
		this.userInput = userInput;
		notify();
	}

	/**
	 * ��ȡ�û�����
	 * 
	 * @return �����û��������ݵ��ַ�����ʽ
	 */
	public synchronized String readInput() {
		String result = null;
		try {
			while (userInput == null) {
				wait();
			}
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		}
		result = userInput;
		userInput = null;
		return result;
	}

	/**
	 * ��������ʱִ�еķ���
	 */
	public void run() {
		table.removeAll();
		statement(root);
		CompilerFrame.problemArea.append("\n");
		CompilerFrame.problemArea.append("**********����������**********\n");
		if (errorNum != 0) {
			CompilerFrame.problemArea.append(errorInfo);
			CompilerFrame.problemArea.append("�ó����й���" + errorNum + "���������\n");
			CompilerFrame.proAndConPanel.setSelectedIndex(1);
			JOptionPane.showMessageDialog(new JPanel(), "��������������ʱ���ִ������޸ģ�",
					"�������", JOptionPane.ERROR_MESSAGE);
		} else {
			CompilerFrame.problemArea.append("�ó����й���" + errorNum + "���������\n");
			CompilerFrame.proAndConPanel.setSelectedIndex(0);
		}
	}

	/**
	 * �������������
	 * 
	 * @param root
	 *            �����
	 */
	private void statement(TreeNode root) {
		for (int i = 0; i < root.getChildCount(); i++) {
			TreeNode currentNode = root.getChildAt(i);
			String content = currentNode.getContent();
			if (content.equals(ConstVar.INT) || content.equals(ConstVar.REAL)
					|| content.equals(ConstVar.BOOL)
					|| content.equals(ConstVar.STRING)) {
				forDeclare(currentNode);
			} else if (content.equals(ConstVar.ASSIGN)) {
				forAssign(currentNode);
			} else if (content.equals(ConstVar.FOR)) {
				// ����forѭ����䣬�ı�������
				level++;
				forFor(currentNode);
				// �˳�forѭ����䣬�ı������򲢸��·��ű�
				level--;
				table.update(level);
			}  else if (content.equals(ConstVar.IF)) {
				// ����if��䣬�ı�������
				level++;
				forIf(currentNode);
				// �˳�if��䣬�ı������򲢸��·��ű�
				level--;
				table.update(level);
			} else if (content.equals(ConstVar.WHILE)) {
				// ����while��䣬�ı�������
				level++;
				forWhile(currentNode);
				// �˳�while��䣬�ı������򲢸��·��ű�
				level--;
				table.update(level);
			} else if (content.equals(ConstVar.READ)) {
				forRead(currentNode.getChildAt(0));
			} else if (content.equals(ConstVar.WRITE)) {
				forWrite(currentNode.getChildAt(0));
			}
		}
	}

	/**
	 * ����declare���
	 * 
	 * @param root
	 *            �����
	 */
	private void forDeclare(TreeNode root) {
		// �����ʾ������,����������������int real bool string
		String content = root.getContent();
		int index = 0;
		while (index < root.getChildCount()) {
			TreeNode temp = root.getChildAt(index);
			// ������
			String name = temp.getContent();
			// �жϱ����Ƿ��Ѿ�������
			if (table.getCurrentLevel(name, level) == null) {
				// ������ͨ����
				if (temp.getChildCount() == 0) {
					SymbolTableElement element = new SymbolTableElement(temp
							.getContent(), content, temp.getLineNum(), level);
					index++;
					// �жϱ����Ƿ�������ʱ����ʼ��
					if (index < root.getChildCount()
							&& root.getChildAt(index).getContent().equals(
									ConstVar.ASSIGN)) {
						// ��ñ����ĳ�ʼֵ���
						TreeNode valueNode = root.getChildAt(index).getChildAt(
								0);
						String value = valueNode.getContent();
						if (content.equals(ConstVar.INT)) { // ����int�ͱ���
							if (matchInteger(value)) {
								element.setIntValue(value);
								element.setRealValue(String.valueOf(Double
										.parseDouble(value)));
							} else if (matchReal(value)) {
								String error = "���ܽ���������ֵ�����ͱ���";
								error(error, valueNode.getLineNum());
							} else if (value.equals("true")
									|| value.equals("false")) {
								String error = "���ܽ�" + value + "��ֵ�����ͱ���";
								error(error, valueNode.getLineNum());
							} else if (valueNode.getNodeKind().equals("�ַ���")) {
								String error = "���ܽ��ַ�����ֵ�����ͱ���";
								error(error, valueNode.getLineNum());
							} else if (valueNode.getNodeKind().equals("��ʶ��")) {
								if (checkID(valueNode, level)) {
									if (table.getAllLevel(
											valueNode.getContent(), level)
											.getKind().equals(ConstVar.INT)) {
										element.setIntValue(table.getAllLevel(
												valueNode.getContent(), level)
												.getIntValue());
										element.setRealValue(table.getAllLevel(
												valueNode.getContent(), level)
												.getRealValue());
									} else if (table.getAllLevel(
											valueNode.getContent(), level)
											.getKind().equals(ConstVar.REAL)) {
										String error = "���ܽ������ͱ�����ֵ�����ͱ���";
										error(error, valueNode.getLineNum());
									} else if (table.getAllLevel(
											valueNode.getContent(), level)
											.getKind().equals(ConstVar.BOOL)) {
										String error = "���ܽ������ͱ�����ֵ�����ͱ���";
										error(error, valueNode.getLineNum());
									} else if (table.getAllLevel(
											valueNode.getContent(), level)
											.getKind().equals(ConstVar.STRING)) {
										String error = "���ܽ��ַ���������ֵ�����ͱ���";
										error(error, valueNode.getLineNum());
									}
								} else {
									return;
								}
							} else if (value.equals(ConstVar.PLUS)
									|| value.equals(ConstVar.MINUS)
									|| value.equals(ConstVar.TIMES)
									|| value.equals(ConstVar.DIVIDE)) {
								String result = forExpression(valueNode);
								if (result != null) {
									if (matchInteger(result)) {
										element.setIntValue(result);
										element.setRealValue(String
												.valueOf(Double
														.parseDouble(result)));
									} else if (matchReal(result)) {
										String error = "���ܽ���������ֵ�����ͱ���";
										error(error, valueNode.getLineNum());
										return;
									} else {
										return;
									}
								} else {
									return;
								}
							}
						} else if (content.equals(ConstVar.REAL)) { // ����real�ͱ���
							if (matchInteger(value)) {
								element.setRealValue(String.valueOf(Double
										.parseDouble(value)));
							} else if (matchReal(value)) {
								element.setRealValue(value);
							} else if (value.equals("true")
									|| value.equals("false")) {
								String error = "���ܽ�" + value + "��ֵ�������ͱ���";
								error(error, valueNode.getLineNum());
							} else if (valueNode.getNodeKind().equals("�ַ���")) {
								String error = "���ܽ��ַ����������ͱ���";
								error(error, valueNode.getLineNum());
							} else if (valueNode.getNodeKind().equals("��ʶ��")) {
								if (checkID(valueNode, level)) {
									if (table.getAllLevel(
											valueNode.getContent(), level)
											.getKind().equals(ConstVar.INT)
											|| table.getAllLevel(
													valueNode.getContent(),
													level).getKind().equals(
													ConstVar.REAL)) {
										element.setRealValue(table.getAllLevel(
												valueNode.getContent(), level)
												.getRealValue());
									} else if (table.getAllLevel(
											valueNode.getContent(), level)
											.getKind().equals(ConstVar.BOOL)) {
										String error = "���ܽ������ͱ�����ֵ�������ͱ���";
										error(error, valueNode.getLineNum());
									} else if (table.getAllLevel(
											valueNode.getContent(), level)
											.getKind().equals(ConstVar.STRING)) {
										String error = "���ܽ��ַ���������ֵ�������ͱ���";
										error(error, valueNode.getLineNum());
									}
								} else {
									return;
								}
							} else if (value.equals(ConstVar.PLUS)
									|| value.equals(ConstVar.MINUS)
									|| value.equals(ConstVar.TIMES)
									|| value.equals(ConstVar.DIVIDE)) {
								String result = forExpression(valueNode);
								if (result != null) {
									if (matchInteger(result)) {
										element.setRealValue(String
												.valueOf(Double
														.parseDouble(result)));
									} else if (matchReal(result)) {
										element.setRealValue(result);
									}
								} else {
									return;
								}
							}
						} else if (content.equals(ConstVar.STRING)) { // ����string�ͱ���
							if (matchInteger(value)) {
								String error = "���ܽ�������ֵ���ַ����ͱ���";
								error(error, valueNode.getLineNum());
							} else if (matchReal(value)) {
								String error = "���ܽ���������ֵ���ַ����ͱ���";
								error(error, valueNode.getLineNum());
							} else if (value.equals("true")
									|| value.equals("false")) {
								String error = "���ܽ�" + value + "��ֵ���ַ����ͱ���";
								error(error, valueNode.getLineNum());
							} else if (valueNode.getNodeKind().equals("�ַ���")) {
								element.setStringValue(value);
							} else if (valueNode.getNodeKind().equals("��ʶ��")) {
								if (checkID(valueNode, level)) {
									if (table.getAllLevel(
											valueNode.getContent(), level)
											.getKind().equals(ConstVar.INT)) {
										String error = "���ܽ�������ֵ���ַ����ͱ���";
										error(error, valueNode.getLineNum());
									} else if (table.getAllLevel(
											valueNode.getContent(), level)
											.getKind().equals(ConstVar.REAL)) {
										String error = "���ܽ���������ֵ���ַ����ͱ���";
										error(error, valueNode.getLineNum());
									} else if (table.getAllLevel(
											valueNode.getContent(), level)
											.getKind().equals(ConstVar.BOOL)) {
										String error = "���ܽ������ͱ�����ֵ���ַ����ͱ���";
										error(error, valueNode.getLineNum());
									} else if (table.getAllLevel(
											valueNode.getContent(), level)
											.getKind().equals(ConstVar.STRING)) {
										element.setStringValue(value);
									}
								} else {
									return;
								}
							} else if (value.equals(ConstVar.PLUS)
									|| value.equals(ConstVar.MINUS)
									|| value.equals(ConstVar.TIMES)
									|| value.equals(ConstVar.DIVIDE)) {
								String error = "���ܽ��������ʽ��ֵ���ַ����ͱ���";
								error(error, valueNode.getLineNum());
							}
						} else { // ����bool�ͱ���
							if (matchInteger(value)) {
								// �����0�������Ϊfalse,������Ϊtrue
								int i = Integer.parseInt(value);
								if (i <= 0)
									element.setStringValue("false");
								else
									element.setStringValue("true");
							} else if (matchReal(value)) {
								String error = "���ܽ���������ֵ�������ͱ���";
								error(error, valueNode.getLineNum());
							} else if (value.equals("true")
									|| value.equals("false")) {
								element.setStringValue(value);
							} else if (valueNode.getNodeKind().equals("�ַ���")) {
								String error = "���ܽ��ַ����������ͱ���";
								error(error, valueNode.getLineNum());
							} else if (valueNode.getNodeKind().equals("��ʶ��")) {
								if (checkID(valueNode, level)) {
									if (table.getAllLevel(
											valueNode.getContent(), level)
											.getKind().equals(ConstVar.INT)) {
										int i = Integer.parseInt(table
												.getAllLevel(
														valueNode.getContent(),
														level).getIntValue());
										if (i <= 0)
											element.setStringValue("false");
										else
											element.setStringValue("true");
									} else if (table.getAllLevel(
											valueNode.getContent(), level)
											.getKind().equals(ConstVar.REAL)) {
										String error = "���ܽ������ͱ�����ֵ�������ͱ���";
										error(error, valueNode.getLineNum());
									} else if (table.getAllLevel(
											valueNode.getContent(), level)
											.getKind().equals(ConstVar.BOOL)) {
										element
												.setStringValue(table
														.getAllLevel(
																valueNode
																		.getContent(),
																level)
														.getStringValue());
									} else if (table.getAllLevel(
											valueNode.getContent(), level)
											.getKind().equals(ConstVar.STRING)) {
										String error = "���ܽ��ַ���������ֵ�������ͱ���";
										error(error, valueNode.getLineNum());
									}
								} else {
									return;
								}
							} else if (value.equals(ConstVar.EQUAL)
									|| value.equals(ConstVar.NEQUAL)
									|| value.equals(ConstVar.LT)
									|| value.equals(ConstVar.GT)) {
								boolean result = forCondition(valueNode);
								if (result) {
									element.setStringValue("true");
								} else {
									element.setStringValue("false");
								}
							}
						}
						index++;
					}
					table.add(element);
				} else { // ��������
					SymbolTableElement element = new SymbolTableElement(temp
							.getContent(), content, temp.getLineNum(), level);
					String sizeValue = temp.getChildAt(0).getContent();
					if (matchInteger(sizeValue)) {
						int i = Integer.parseInt(sizeValue);
						if (i < 1) {
							String error = "�����С���������";
							error(error, root.getLineNum());
							return;
						}
					} else if (temp.getChildAt(0).getNodeKind().equals("��ʶ��")) {
						if (checkID(root, level)) {
							SymbolTableElement tempElement = table.getAllLevel(
									root.getContent(), level);
							if (tempElement.getKind().equals(ConstVar.INT)) {
								int i = Integer.parseInt(tempElement
										.getIntValue());
								if (i < 1) {
									String error = "�����С���������";
									error(error, root.getLineNum());
									return;
								} else {
									sizeValue = tempElement.getIntValue();
								}
							} else {
								String error = "���Ͳ�ƥ��,�����С����Ϊ��������";
								error(error, root.getLineNum());
								return;
							}
						} else {
							return;
						}
					} else if (sizeValue.equals(ConstVar.PLUS)
							|| sizeValue.equals(ConstVar.MINUS)
							|| sizeValue.equals(ConstVar.TIMES)
							|| sizeValue.equals(ConstVar.DIVIDE)) {
						sizeValue = forExpression(temp.getChildAt(0));
						if (sizeValue != null) {
							if (matchInteger(sizeValue)) {
								int i = Integer.parseInt(sizeValue);
								if (i < 1) {
									String error = "�����С���������";
									error(error, root.getLineNum());
									return;
								}
							} else {
								String error = "���Ͳ�ƥ��,�����С����Ϊ��������";
								error(error, root.getLineNum());
								return;
							}
						} else {
							return;
						}
					}
					element.setArrayElementsNum(Integer.parseInt(sizeValue));
					table.add(element);
					index++;
					for (int j = 0; j < Integer.parseInt(sizeValue); j++) {
						String s = temp.getContent() + "@" + j;
						SymbolTableElement ste = new SymbolTableElement(s,
								content, temp.getLineNum(), level);
						table.add(ste);
					}
				}
			} else { // ����
				String error = "����" + name + "�ѱ�����,���������ñ���";
				error(error, temp.getLineNum());
				return;
			}
		}
	}

	/**
	 * ����assign���
	 * 
	 * @param root
	 *            �﷨����assign�����
	 */
	private void forAssign(TreeNode root) {
		// ��ֵ�����벿��
		TreeNode node1 = root.getChildAt(0);
		// ��ֵ�����벿�ֱ�ʶ��
		String node1Value = node1.getContent();
		if (table.getAllLevel(node1Value, level) != null) {
			if (node1.getChildCount() != 0) {
				String s = forArray(node1.getChildAt(0), table.getAllLevel(
						node1Value, level).getArrayElementsNum());
				if (s != null)
					node1Value += "@" + s;
				else
					return;
			}
		} else {
			String error = "����" + node1Value + "��ʹ��ǰδ����";
			error(error, node1.getLineNum());
			return;
		}
		// ��ֵ�����벿�ֱ�ʶ������
		String node1Kind = table.getAllLevel(node1Value, level).getKind();
		// ��ֵ����Ұ벿��
		TreeNode node2 = root.getChildAt(1);
		String node2Kind = node2.getNodeKind();
		String node2Value = node2.getContent();
		// ��ֵ����Ұ벿�ֵ�ֵ
		String value = "";
		if (node2Kind.equals("����")) { // ����
			value = node2Value;
			node2Kind = "int";
		} else if (node2Kind.equals("ʵ��")) { // ʵ��
			value = node2Value;
			node2Kind = "real";
		} else if (node2Kind.equals("�ַ���")) { // �ַ���
			value = node2Value;
			node2Kind = "string";
		} else if (node2Kind.equals("����ֵ")) { // true��false
			value = node2Value;
			node2Kind = "bool";
		} else if (node2Kind.equals("��ʶ��")) { // ��ʶ��
			if (checkID(node2, level)) {
				if (node2.getChildCount() != 0) {
					String s = forArray(node2.getChildAt(0), table.getAllLevel(
							node2Value, level).getArrayElementsNum());
					if (s != null)
						node2Value += "@" + s;
					else
						return;
				}
				SymbolTableElement temp = table.getAllLevel(node2Value, level);
				if (temp.getKind().equals(ConstVar.INT)) {
					value = temp.getIntValue();
				} else if (temp.getKind().equals(ConstVar.REAL)) {
					value = temp.getRealValue();
				} else if (temp.getKind().equals(ConstVar.BOOL)
						|| temp.getKind().equals(ConstVar.STRING)) {
					value = temp.getStringValue();
				}
				node2Kind = table.getAllLevel(node2Value, level).getKind();
			} else {
				return;
			}
		} else if (node2Value.equals(ConstVar.PLUS)
				|| node2Value.equals(ConstVar.MINUS)
				|| node2Value.equals(ConstVar.TIMES)
				|| node2Value.equals(ConstVar.DIVIDE)) { // ���ʽ
			String result = forExpression(node2);
			if (result != null) {
				if (matchInteger(result))
					node2Kind = "int";
				else if (matchReal(result))
					node2Kind = "real";
				value = result;
			} else {
				return;
			}
		} else if (node2Value.equals(ConstVar.EQUAL)
				|| node2Value.equals(ConstVar.NEQUAL)
				|| node2Value.equals(ConstVar.LT)
				|| node2Value.equals(ConstVar.GT)) { // �߼����ʽ
			boolean result = forCondition(node2);
			node2Kind = "bool";
			value = String.valueOf(result);
		}
		if (node1Kind.equals(ConstVar.INT)) {
			if (node2Kind.equals(ConstVar.INT)) {
				table.getAllLevel(node1Value, level).setIntValue(value);
				table.getAllLevel(node1Value, level).setRealValue(
						String.valueOf(Double.parseDouble(value)));
			} else if (node2Kind.equals(ConstVar.REAL)) {
				String error = "���ܽ���������ֵ�����ͱ���";
				error(error, node1.getLineNum());
				return;
			} else if (node2Kind.equals(ConstVar.BOOL)) {
				String error = "���ܽ�����ֵ��ֵ�����ͱ���";
				error(error, node1.getLineNum());
				return;
			} else if (node2Kind.equals(ConstVar.STRING)) {
				String error = "���ܽ��ַ��������ͱ���";
				error(error, node1.getLineNum());
				return;
			}
		} else if (node1Kind.equals(ConstVar.REAL)) {
			if (node2Kind.equals(ConstVar.INT)) {
				table.getAllLevel(node1Value, level).setRealValue(
						String.valueOf(Double.parseDouble(value)));
			} else if (node2Kind.equals(ConstVar.REAL)) {
				table.getAllLevel(node1Value, level).setRealValue(value);
			} else if (node2Kind.equals(ConstVar.BOOL)) {
				String error = "���ܽ�����ֵ��ֵ�������ͱ���";
				error(error, node1.getLineNum());
				return;
			} else if (node2Kind.equals(ConstVar.STRING)) {
				String error = "���ܽ��ַ����������ͱ���";
				error(error, node1.getLineNum());
				return;
			}
		} else if (node1Kind.equals(ConstVar.BOOL)) {
			if (node2Kind.equals(ConstVar.INT)) {
				int i = Integer.parseInt(node2Value);
				if (i <= 0)
					table.getAllLevel(node1Value, level).setStringValue("false");
				else
					table.getAllLevel(node1Value, level).setStringValue("true");
			} else if (node2Kind.equals(ConstVar.REAL)) {
				String error = "���ܽ���������ֵ�������ͱ���";
				error(error, node1.getLineNum());
				return;
			} else if (node2Kind.equals(ConstVar.BOOL)) {
				table.getAllLevel(node1Value, level).setStringValue(value);
			} else if (node2Kind.equals(ConstVar.STRING)) {
				String error = "���ܽ��ַ�����ֵ�������ͱ���";
				error(error, node1.getLineNum());
				return;
			}
		} else if (node1Kind.equals(ConstVar.STRING)) {
			if (node2Kind.equals(ConstVar.INT)) {
				String error = "���ܽ�������ֵ���ַ�������";
				error(error, node1.getLineNum());
				return;
			} else if (node2Kind.equals(ConstVar.REAL)) {
				String error = "���ܽ���������ֵ���ַ�������";
				error(error, node1.getLineNum());
				return;
			} else if (node2Kind.equals(ConstVar.BOOL)) {
				String error = "���ܽ�����������ֵ���ַ�������";
				error(error, node1.getLineNum());
				return;
			} else if (node2Kind.equals(ConstVar.STRING)) {
				table.getAllLevel(node1Value, level).setStringValue(value);
			}
		}
	}

	/**
	 * ����for���
	 * 
	 * @param root
	 *            �﷨����for�����
	 */
	private void forFor(TreeNode root) {
		// �����Initialization
		TreeNode initializationNode = root.getChildAt(0);
		// �����Condition
		TreeNode conditionNode = root.getChildAt(1);
		// �����Change
		TreeNode changeNode = root.getChildAt(2);
		// �����Statements
		TreeNode statementNode = root.getChildAt(3);
		// forѭ������ʼ��
		forAssign(initializationNode.getChildAt(0));
		// ����Ϊ��
		while (forCondition(conditionNode.getChildAt(0))) {
			statement(statementNode);
			level--;
			table.update(level);
			level++;
			// forѭ��ִ��һ�κ�ı�ѭ�������еı���
			forAssign(changeNode.getChildAt(0));
		}
	}
	
	/**
	 * ����if���
	 * 
	 * @param root
	 *            �﷨����if�����
	 */
	private void forIf(TreeNode root) {
		int count = root.getChildCount();
		// �����Condition
		TreeNode conditionNode = root.getChildAt(0);
		// �����Statements
		TreeNode statementNode = root.getChildAt(1);
		// ����Ϊ��
		if (forCondition(conditionNode.getChildAt(0))) {
			statement(statementNode);
		} else if (count == 3) { // ����Ϊ������else���
			TreeNode elseNode = root.getChildAt(2);
			level++;
			statement(elseNode);
			level--;
			table.update(level);
		} else { // ����Ϊ��ͬʱû��else���
			return;
		}
	}

	/**
	 * ����while���
	 * 
	 * @param root
	 *            �﷨����while�����
	 */
	private void forWhile(TreeNode root) {
		// �����Condition
		TreeNode conditionNode = root.getChildAt(0);
		// �����Statements
		TreeNode statementNode = root.getChildAt(1);
		while (forCondition(conditionNode.getChildAt(0))) {
			statement(statementNode);
			level--;
			table.update(level);
			level++;
		}
	}

	/**
	 * ����read���
	 * 
	 * @param root
	 *            �﷨����read�����
	 */
	private void forRead(TreeNode root) {
//		CompilerFrame.consoleArea.setText("");
		CompilerFrame.setControlArea(Color.GREEN, true);
		// Ҫ��ȡ�ı���������
		String idName = root.getContent();
		// ���ұ���
		SymbolTableElement element = table.getAllLevel(idName, level);
		// �жϱ����Ƿ��Ѿ�����
		if (element != null) {
			if (root.getChildCount() != 0) {
				String s = forArray(root.getChildAt(0), element
						.getArrayElementsNum());
				if (s != null) {
					idName += "@" + s;
				} else {
					return;
				}
			}
			String value = readInput();
			if (element.getKind().equals(ConstVar.INT)) {
				if (matchInteger(value)) {
					table.getAllLevel(idName, level).setIntValue(value);
					table.getAllLevel(idName, level).setRealValue(
							String.valueOf(Double.parseDouble(value)));
				} else { // ����
					String error = "���ܽ�\"" + value + "\"��ֵ������" + idName;
					JOptionPane.showMessageDialog(new JPanel(), error, "�������",
							JOptionPane.ERROR_MESSAGE);
				}
			} else if (element.getKind().equals(ConstVar.REAL)) {
				if (matchReal(value)) {
					table.getAllLevel(idName, level).setRealValue(value);
				} else if (matchInteger(value)) {
					table.getAllLevel(idName, level).setRealValue(
							String.valueOf(Double.parseDouble(value)));
				} else { // ����
					String error = "���ܽ�\"" + value + "\"��ֵ������" + idName;
					JOptionPane.showMessageDialog(new JPanel(), error, "�������",
							JOptionPane.ERROR_MESSAGE);
				}
			} else if (element.getKind().equals(ConstVar.BOOL)) {
				if (value.equals("true")) {
					table.getAllLevel(idName, level).setStringValue("true");
				} else if (value.equals("false")) {
					table.getAllLevel(idName, level).setStringValue("false");
				} else { // ����
					String error = "���ܽ�\"" + value + "\"��ֵ������" + idName;
					JOptionPane.showMessageDialog(new JPanel(), error, "�������",
							JOptionPane.ERROR_MESSAGE);
				}
			} else if (element.getKind().equals(ConstVar.STRING)) {
				table.getAllLevel(idName, level).setStringValue(value);
			}
		} else { // ����
			String error = "����" + idName + "��ʹ��ǰδ����";
			error(error, root.getLineNum());
		}
	}

	/**
	 * ����write���
	 * 
	 * @param root
	 *            �﷨����write�����
	 */
	private void forWrite(TreeNode root) {
		CompilerFrame.setControlArea(Color.BLACK, false);
		// �����ʾ������
		String content = root.getContent();
		// ��������
		String kind = root.getNodeKind();
		if (kind.equals("����") || kind.equals("ʵ��")) { // ����
			CompilerFrame.consoleArea.setText(CompilerFrame.consoleArea
					.getText()
					+ content + "\n");
		} else if (kind.equals("�ַ���")) { // �ַ���
			CompilerFrame.consoleArea.setText(CompilerFrame.consoleArea
					.getText()
					+ content + "\n");
		} else if (kind.equals("��ʶ��")) { // ��ʶ��
			if (checkID(root, level)) {
				if (root.getChildCount() != 0) {
					String s = forArray(root.getChildAt(0), table.getAllLevel(
							content, level).getArrayElementsNum());
					if (s != null)
						content += "@" + s;
					else
						return;
				}
				SymbolTableElement temp = table.getAllLevel(content, level);
				if (temp.getKind().equals(ConstVar.INT)) {
					CompilerFrame.consoleArea.setText(CompilerFrame.consoleArea
							.getText()
							+ temp.getIntValue() + "\n");
				} else if (temp.getKind().equals(ConstVar.REAL)) {
					CompilerFrame.consoleArea.setText(CompilerFrame.consoleArea
							.getText()
							+ temp.getRealValue() + "\n");
				} else {
					CompilerFrame.consoleArea.setText(CompilerFrame.consoleArea
							.getText()
							+ temp.getStringValue() + "\n");
				}
			} else {
				return;
			}
		} else if (content.equals(ConstVar.PLUS)
				|| content.equals(ConstVar.MINUS)
				|| content.equals(ConstVar.TIMES)
				|| content.equals(ConstVar.DIVIDE)) { // ���ʽ
			String value = forExpression(root);
			if (value != null) {
				CompilerFrame.consoleArea.setText(CompilerFrame.consoleArea
						.getText()
						+ value + "\n");
			}
		}
	}

	/**
	 * ����if��while��������
	 * 
	 * @param root
	 *            �����
	 * @return ���ؼ�����
	 */
	private boolean forCondition(TreeNode root) {
		// > < <> == true false ��������
		String content = root.getContent();
		if (content.equals(ConstVar.TRUE)) {
			return true;
		} else if (content.equals(ConstVar.FALSE)) {
			return false;
		} else if (root.getNodeKind().equals("��ʶ��")) {
			if (checkID(root, level)) {
				if (root.getChildCount() != 0) {
					String s = forArray(root.getChildAt(0), table.getAllLevel(
							content, level).getArrayElementsNum());
					if (s != null)
						content += "@" + s;
					else
						return false;
				}
				SymbolTableElement temp = table.getAllLevel(content, level);
				if (temp.getKind().equals(ConstVar.BOOL)) {
					if(temp.getStringValue().equals(ConstVar.TRUE))
						return true;
					else
						return false;
				} else { // ����
					String error = "���ܽ�����" + content + "��Ϊ�ж�����";
					error(error, root.getLineNum());
				}
			} else {
				return false;
			}
		} else if (content.equals(ConstVar.EQUAL)
				|| content.equals(ConstVar.NEQUAL)
				|| content.equals(ConstVar.LT) || content.equals(ConstVar.GT)) {
			// ����������Ƚ϶����ֵ
			String[] results = new String[2];
			for (int i = 0; i < root.getChildCount(); i++) {
				String kind = root.getChildAt(i).getNodeKind();
				String tempContent = root.getChildAt(i).getContent();
				if (kind.equals("����") || kind.equals("ʵ��")) { // ����
					results[i] = tempContent;
				} else if (kind.equals("��ʶ��")) { // ��ʶ��
					if (checkID(root.getChildAt(i), level)) {
						if (root.getChildAt(i).getChildCount() != 0) {
							String s = forArray(root.getChildAt(i)
									.getChildAt(0), table.getAllLevel(
									tempContent, level).getArrayElementsNum());
							if (s != null)
								tempContent += "@" + s;
							else
								return false;
						}
						SymbolTableElement temp = table.getAllLevel(
								tempContent, level);
						if (temp.getKind().equals(ConstVar.INT)) {
							results[i] = temp.getIntValue();
						} else {
							results[i] = temp.getRealValue();
						}
					} else {
						return false;
					}
				} else if (tempContent.equals(ConstVar.PLUS)
						|| tempContent.equals(ConstVar.MINUS)
						|| tempContent.equals(ConstVar.TIMES)
						|| tempContent.equals(ConstVar.DIVIDE)) { // ���ʽ
					String result = forExpression(root.getChildAt(i));
					if (result != null)
						results[i] = result;
					else
						return false;
				}
			}
			if (!results[0].equals("") && !results[1].equals("")) {
				double element1 = Double.parseDouble(results[0]);
				double element2 = Double.parseDouble(results[1]);
				if (content.equals(ConstVar.GT)) { // >
					if (element1 > element2)
						return true;
				} else if (content.equals(ConstVar.LT)) { // <
					if (element1 < element2)
						return true;
				} else if (content.equals(ConstVar.EQUAL)) { // ==
					if (element1 == element2)
						return true;
				} else { // <>
					if (element1 != element2)
						return true;
				}
			}
		}
		// �������������߷����������Ϊ�ٷ���false
		return false;
	}

	/**
	 * �������ʽ
	 * 
	 * @param root
	 *            �����
	 * @return ���ؼ�����
	 */
	private String forExpression(TreeNode root) {
		boolean isInt = true;
		// + -
		String content = root.getContent();
		// ���������������ֵ
		String[] results = new String[2];
		for (int i = 0; i < root.getChildCount(); i++) {
			TreeNode tempNode = root.getChildAt(i);
			String kind = tempNode.getNodeKind();
			String tempContent = tempNode.getContent();
			if (kind.equals("����")) { // ����
				results[i] = tempContent;
			} else if (kind.equals("ʵ��")) { // ʵ��
				results[i] = tempContent;
				isInt = false;
			} else if (kind.equals("��ʶ��")) { // ��ʶ��
				if (checkID(tempNode, level)) {
					if (tempNode.getChildCount() != 0) {
						String s = forArray(tempNode.getChildAt(0), table
								.getAllLevel(tempContent, level)
								.getArrayElementsNum());
						if (s != null)
							tempContent += "@" + s;
						else
							return null;
					}
					SymbolTableElement temp = table.getAllLevel(tempNode
							.getContent(), level);
					if (temp.getKind().equals(ConstVar.INT)) {
						results[i] = temp.getIntValue();
					} else if (temp.getKind().equals(ConstVar.REAL)) {
						results[i] = temp.getRealValue();
						isInt = false;
					}
				} else {
					return null;
				}
			} else if (tempContent.equals(ConstVar.PLUS)
					|| tempContent.equals(ConstVar.MINUS)
					|| tempContent.equals(ConstVar.TIMES)
					|| tempContent.equals(ConstVar.DIVIDE)) { // ���ʽ
				String result = forExpression(root.getChildAt(i));
				if (result != null) {
					results[i] = result;
					if (matchReal(result))
						isInt = false;
				} else
					return null;
			}
		}
		if (isInt) {
			int e1 = Integer.parseInt(results[0]);
			int e2 = Integer.parseInt(results[1]);
			if (content.equals(ConstVar.PLUS))
				return String.valueOf(e1 + e2);
			else if (content.equals(ConstVar.MINUS))
				return String.valueOf(e1 - e2);
			else if (content.equals(ConstVar.TIMES))
				return String.valueOf(e1 * e2);
			else
				return String.valueOf(e1 / e2);
		} else {
			double e1 = Double.parseDouble(results[0]);
			double e2 = Double.parseDouble(results[1]);
			BigDecimal bd1 = new BigDecimal(e1);
			BigDecimal bd2 = new BigDecimal(e2);
			if (content.equals(ConstVar.PLUS))
				return String.valueOf(bd1.add(bd2).floatValue());
			else if (content.equals(ConstVar.MINUS))
				return String.valueOf(bd1.subtract(bd2).floatValue());
			else if (content.equals(ConstVar.TIMES))
				return String.valueOf(bd1.multiply(bd2).floatValue());
			else
				return String.valueOf(bd1.divide(bd2, 3,
						BigDecimal.ROUND_HALF_UP).floatValue());
		}
	}

	/**
	 * array
	 * 
	 * @param root
	 *            �����
	 * @param arraySize
	 *            �����С
	 * @return ������null
	 */
	private String forArray(TreeNode root, int arraySize) {
		if (root.getNodeKind().equals("����")) {
			int i = Integer.parseInt(root.getContent());
			if (i > -1 && i < arraySize) {
				return root.getContent();
			} else if (i < 0) {
				String error = "�����±겻��Ϊ����";
				error(error, root.getLineNum());
				return null;
			} else {
				String error = "�����±�Խ��";
				error(error, root.getLineNum());
				return null;
			}
		} else if (root.getNodeKind().equals("��ʶ��")) {
			// ����ʶ��
			if (checkID(root, level)) {
				SymbolTableElement temp = table.getAllLevel(root.getContent(),
						level);
				if (temp.getKind().equals(ConstVar.INT)) {
					int i = Integer.parseInt(temp.getIntValue());
					if (i > -1 && i < arraySize) {
						return temp.getIntValue();
					} else if (i < 0) {
						String error = "�����±겻��Ϊ����";
						error(error, root.getLineNum());
						return null;
					} else {
						String error = "�����±�Խ��";
						error(error, root.getLineNum());
						return null;
					}
				} else {
					String error = "���Ͳ�ƥ��,���������ű���Ϊ��������";
					error(error, root.getLineNum());
					return null;
				}
			} else {
				return null;
			}
		} else if (root.getContent().equals(ConstVar.PLUS)
				|| root.getContent().equals(ConstVar.MINUS)
				|| root.getContent().equals(ConstVar.TIMES)
				|| root.getContent().equals(ConstVar.DIVIDE)) { // ���ʽ
			String result = forExpression(root);
			if (result != null) {
				if (matchInteger(result)) {
					int i = Integer.parseInt(result);
					if (i > -1 && i < arraySize) {
						return result;
					} else if (i < 0) {
						String error = "�����±겻��Ϊ����";
						error(error, root.getLineNum());
						return null;
					} else {
						String error = "�����±�Խ��";
						error(error, root.getLineNum());
						return null;
					}
				} else {
					String error = "���Ͳ�ƥ��,���������ű���Ϊ��������";
					error(error, root.getLineNum());
					return null;
				}
			} else
				return null;
		}
		return null;
	}

	/**
	 * ����ַ����Ƿ������ͳ�ʼ��
	 * 
	 * @param root
	 *            �ַ������
	 * @param level
	 *            �ַ���������
	 * @return ��������ҳ�ʼ���򷵻�true,���򷵻�false
	 */
	private boolean checkID(TreeNode root, int level) {
		// ��ʶ������
		String idName = root.getContent();
		// ��ʶ��δ����
		if (table.getAllLevel(idName, level) == null) {
			String error = "����" + idName + "��ʹ��ǰδ����";
			error(error, root.getLineNum());
			return false;
		} else {
			if (root.getChildCount() != 0) {
				String tempString = forArray(root.getChildAt(0), table
						.getAllLevel(idName, level).getArrayElementsNum());
				if (tempString != null)
					idName += "@" + tempString;
				else
					return false;
			}
			SymbolTableElement temp = table.getAllLevel(idName, level);
			// ����δ��ʼ��
			if (temp.getIntValue().equals("") && temp.getRealValue().equals("")
					&& temp.getStringValue().equals("")) {
				String error = "����" + idName + "��ʹ��ǰδ��ʼ��";
				error(error, root.getLineNum());
				return false;
			} else {
				return true;
			}
		}
	}

	public String getErrorInfo() {
		return errorInfo;
	}

	public void setErrorInfo(String errorInfo) {
		this.errorInfo = errorInfo;
	}

	public int getErrorNum() {
		return errorNum;
	}

	public void setErrorNum(int errorNum) {
		this.errorNum = errorNum;
	}

}