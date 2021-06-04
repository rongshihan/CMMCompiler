package compiler;

import java.util.ArrayList;

import structure.ConstVar;
import structure.Token;
import structure.TreeNode;

/**
 * CMM�﷨������
 */
public class CMMParser {

	// �ʷ������õ���tokens����
	private ArrayList<Token> tokens;
	// ��ǵ�ǰtoken���α�
	private int index = 0;
	// ��ŵ�ǰtoken��ֵ
	private Token currentToken = null;
	// �������
	private int errorNum = 0;
	// ������Ϣ
	private String errorInfo = "";
	// �﷨���������
	private static TreeNode root;

	public CMMParser(ArrayList<Token> tokens) {
		this.tokens = tokens;
		if (tokens.size() != 0)
			currentToken = tokens.get(0);
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

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	/**
	 * �﷨����������
	 * 
	 * @return TreeNode
	 */
	public TreeNode execute() {
		root = new TreeNode("PROGRAM");
		for (; index < tokens.size();) {
			root.add(statement());
		}
		return root;
	}

	/**
	 * ȡ��tokens�е���һ��token
	 * 
	 */
	private void nextToken() {
		index++;
		if (index > tokens.size() - 1) {
			currentToken = null;
			if (index > tokens.size())
				index--;
			return;
		}
		currentToken = tokens.get(index);
	}

	/**
	 * ��������
	 * 
	 * @param error
	 *            ������Ϣ
	 */
	private void error(String error) {
		String line = "    ERROR:�� ";
		Token previous = tokens.get(index - 1);
		if (currentToken != null
				&& currentToken.getLine() == previous.getLine()) {
			line += currentToken.getLine() + " ��,�� " + currentToken.getCulomn()
					+ " �У�";
		} else
			line += previous.getLine() + " ��,�� " + previous.getCulomn() + " �У�";
		errorInfo += line + error;
		errorNum++;
	}

	/**
	 * statement: if_stm | while_stm | read_stm | write_stm | assign_stm |
	 * declare_stm | for_stm;
	 * 
	 * @return TreeNode
	 */
	private final TreeNode statement() {
		// ����Ҫ���صĽ��
		TreeNode tempNode = null;
		// ��ֵ���
		if (currentToken != null && currentToken.getKind().equals("��ʶ��")) {
			tempNode = assign_stm(false);
		}
		// �������
		else if (currentToken != null
				&& (currentToken.getContent().equals(ConstVar.INT)
						|| currentToken.getContent().equals(ConstVar.REAL) || currentToken
						.getContent().equals(ConstVar.BOOL))
				|| currentToken.getContent().equals(ConstVar.STRING)) {
			tempNode = declare_stm();
		}
		// Forѭ�����
		else if (currentToken != null
				&& currentToken.getContent().equals(ConstVar.FOR)) {
			tempNode = for_stm();
		}
		// If�������
		else if (currentToken != null
				&& currentToken.getContent().equals(ConstVar.IF)) {
			tempNode = if_stm();
		}
		// Whileѭ�����
		else if (currentToken != null
				&& currentToken.getContent().equals(ConstVar.WHILE)) {
			tempNode = while_stm();
		}
		// read���
		else if (currentToken != null
				&& currentToken.getContent().equals(ConstVar.READ)) {
			TreeNode readNode = new TreeNode("�ؼ���", ConstVar.READ, currentToken
					.getLine());
			readNode.add(read_stm());
			tempNode = readNode;
		}
		// write���
		else if (currentToken != null
				&& currentToken.getContent().equals(ConstVar.WRITE)) {
			TreeNode writeNode = new TreeNode("�ؼ���", ConstVar.WRITE,
					currentToken.getLine());
			writeNode.add(write_stm());
			tempNode = writeNode;
		}
		// ������
		else {
			String error = " ����Դ����token��ʼ" + "\n";
			error(error);
			tempNode = new TreeNode(ConstVar.ERROR + "����Դ����token��ʼ");
			nextToken();
		}
		return tempNode;
	}

	/**
	 * for_stm :FOR LPAREN (assign_stm) SEMICOLON condition SEMICOLON assign_stm
	 * RPAREN LBRACE statement RBRACE;
	 * 
	 * @return TreeNode
	 */
	private final TreeNode for_stm() {
		// �Ƿ��д�����,Ĭ��Ϊtrue
		boolean hasBrace = true;
		// if�������ؽ��ĸ����
		TreeNode forNode = new TreeNode("�ؼ���", "for", currentToken.getLine());
		nextToken();
		// ƥ��������(
		if (currentToken != null
				&& currentToken.getContent().equals(ConstVar.LPAREN)) {
			nextToken();
		} else { // ����
			String error = " forѭ�����ȱ��������\"(\"" + "\n";
			error(error);
			forNode.add(new TreeNode(ConstVar.ERROR + "forѭ�����ȱ��������\"(\""));
		}
		// initialization
		TreeNode initializationNode = new TreeNode("initialization",
				"Initialization", currentToken.getLine());
		initializationNode.add(assign_stm(true));
		forNode.add(initializationNode);
		// ƥ��ֺ�;
		if (currentToken != null
				&& currentToken.getContent().equals(ConstVar.SEMICOLON)) {
			nextToken();
		} else {
			String error = " forѭ�����ȱ�ٷֺ�\";\"" + "\n";
			error(error);
			return new TreeNode(ConstVar.ERROR + "forѭ�����ȱ�ٷֺ�\";\"");
		}
		// condition
		TreeNode conditionNode = new TreeNode("condition", "Condition",
				currentToken.getLine());
		conditionNode.add(condition());
		forNode.add(conditionNode);
		// ƥ��ֺ�;
		if (currentToken != null
				&& currentToken.getContent().equals(ConstVar.SEMICOLON)) {
			nextToken();
		} else {
			String error = " forѭ�����ȱ�ٷֺ�\";\"" + "\n";
			error(error);
			return new TreeNode(ConstVar.ERROR + "forѭ�����ȱ�ٷֺ�\";\"");
		}
		// change
		TreeNode changeNode = new TreeNode("change", "Change", currentToken
				.getLine());
		changeNode.add(assign_stm(true));
		forNode.add(changeNode);
		// ƥ��������)
		if (currentToken != null
				&& currentToken.getContent().equals(ConstVar.RPAREN)) {
			nextToken();
		} else { // ����
			String error = " if�������ȱ��������\")\"" + "\n";
			error(error);
			forNode.add(new TreeNode(ConstVar.ERROR + "if�������ȱ��������\")\""));
		}
		// ƥ���������{
		if (currentToken != null
				&& currentToken.getContent().equals(ConstVar.LBRACE)) {
			nextToken();
		} else {
			hasBrace = false;
		}
		// statement
		TreeNode statementNode = new TreeNode("statement", "Statements",
				currentToken.getLine());
		forNode.add(statementNode);
		if(hasBrace) {
		while (currentToken != null) {
			if (!currentToken.getContent().equals(ConstVar.RBRACE))
				statementNode.add(statement());
			else if (statementNode.getChildCount() == 0) {
				forNode.remove(forNode.getChildCount() - 1);
				statementNode.setContent("EmptyStm");
				forNode.add(statementNode);
				break;
			} else {
				break;
			}
		}
		// ƥ���Ҵ�����}
		if (currentToken != null
				&& currentToken.getContent().equals(ConstVar.RBRACE)) {
			nextToken();
		} else { // ����
			String error = " if�������ȱ���Ҵ�����\"}\"" + "\n";
			error(error);
			forNode.add(new TreeNode(ConstVar.ERROR + "if�������ȱ���Ҵ�����\"}\""));
		}
		} else {
			statementNode.add(statement());
		}
		return forNode;
	}

	/**
	 * if_stm: IF LPAREN condition RPAREN LBRACE statement RBRACE (ELSE LBRACE
	 * statement RBRACE)?;
	 * 
	 * @return TreeNode
	 */
	private final TreeNode if_stm() {
		// if����Ƿ��д�����,Ĭ��Ϊtrue
		boolean hasIfBrace = true;
		// else����Ƿ��д�����,Ĭ��Ϊtrue
		boolean hasElseBrace = true;
		// if�������ؽ��ĸ����
		TreeNode ifNode = new TreeNode("�ؼ���", "if", currentToken.getLine());
		nextToken();
		// ƥ��������(
		if (currentToken != null
				&& currentToken.getContent().equals(ConstVar.LPAREN)) {
			nextToken();
		} else { // ����
			String error = " if�������ȱ��������\"(\"" + "\n";
			error(error);
			ifNode.add(new TreeNode(ConstVar.ERROR + "if�������ȱ��������\"(\""));
		}
		// condition
		TreeNode conditionNode = new TreeNode("condition", "Condition",
				currentToken.getLine());
		ifNode.add(conditionNode);
		conditionNode.add(condition());
		// ƥ��������)
		if (currentToken != null
				&& currentToken.getContent().equals(ConstVar.RPAREN)) {
			nextToken();
		} else { // ����
			String error = " if�������ȱ��������\")\"" + "\n";
			error(error);
			ifNode.add(new TreeNode(ConstVar.ERROR + "if�������ȱ��������\")\""));
		}
		// ƥ���������{
		if (currentToken != null
				&& currentToken.getContent().equals(ConstVar.LBRACE)) {
			nextToken();
		} else {
			hasIfBrace = false;
		}
		// statement
		TreeNode statementNode = new TreeNode("statement", "Statements",
				currentToken.getLine());
		ifNode.add(statementNode);
		if (hasIfBrace) {
			while (currentToken != null) {
				if (!currentToken.getContent().equals(ConstVar.RBRACE))
					statementNode.add(statement());
				else if (statementNode.getChildCount() == 0) {
					ifNode.remove(ifNode.getChildCount() - 1);
					statementNode.setContent("EmptyStm");
					ifNode.add(statementNode);
					break;
				} else {
					break;
				}
			}
			// ƥ���Ҵ�����}
			if (currentToken != null
					&& currentToken.getContent().equals(ConstVar.RBRACE)) {
				nextToken();
			} else { // ����
				String error = " if�������ȱ���Ҵ�����\"}\"" + "\n";
				error(error);
				ifNode.add(new TreeNode(ConstVar.ERROR + "if�������ȱ���Ҵ�����\"}\""));
			}
		} else {
			if (currentToken != null)
				statementNode.add(statement());
		}
		if (currentToken != null
				&& currentToken.getContent().equals(ConstVar.ELSE)) {
			TreeNode elseNode = new TreeNode("�ؼ���", ConstVar.ELSE, currentToken
					.getLine());
			ifNode.add(elseNode);
			nextToken();
			// ƥ���������{
			if (currentToken.getContent().equals(ConstVar.LBRACE)) {
				nextToken();
			} else {
				hasElseBrace = false;
			}
			if (hasElseBrace) {
				// statement
				while (currentToken != null
						&& !currentToken.getContent().equals(ConstVar.RBRACE)) {
					elseNode.add(statement());
				}
				// ƥ���Ҵ�����}
				if (currentToken != null
						&& currentToken.getContent().equals(ConstVar.RBRACE)) {
					nextToken();
				} else { // ����
					String error = " else���ȱ���Ҵ�����\"}\"" + "\n";
					error(error);
					elseNode.add(new TreeNode(ConstVar.ERROR
							+ "else���ȱ���Ҵ�����\"}\""));
				}
			} else {
				if (currentToken != null)
				elseNode.add(statement());
			}
		}
		return ifNode;
	}

	/**
	 * while_stm: WHILE LPAREN condition RPAREN LBRACE statement RBRACE;
	 * 
	 * @return TreeNode
	 */
	private final TreeNode while_stm() {
		// �Ƿ��д�����,Ĭ��Ϊtrue
		boolean hasBrace = true;
		// while�������ؽ��ĸ����
		TreeNode whileNode = new TreeNode("�ؼ���", ConstVar.WHILE, currentToken
				.getLine());
		nextToken();
		// ƥ��������(
		if (currentToken != null
				&& currentToken.getContent().equals(ConstVar.LPAREN)) {
			nextToken();
		} else { // ����
			String error = " whileѭ��ȱ��������\"(\"" + "\n";
			error(error);
			whileNode.add(new TreeNode(ConstVar.ERROR + "whileѭ��ȱ��������\"(\""));
		}
		// condition
		TreeNode conditionNode = new TreeNode("condition", "Condition",
				currentToken.getLine());
		whileNode.add(conditionNode);
		conditionNode.add(condition());
		// ƥ��������)
		if (currentToken != null
				&& currentToken.getContent().equals(ConstVar.RPAREN)) {
			nextToken();
		} else { // ����
			String error = " whileѭ��ȱ��������\")\"" + "\n";
			error(error);
			whileNode.add(new TreeNode(ConstVar.ERROR + "whileѭ��ȱ��������\")\""));
		}
		// ƥ���������{
		if (currentToken != null
				&& currentToken.getContent().equals(ConstVar.LBRACE)) {
			nextToken();
		} else {
			hasBrace = false;
		}
		// statement
		TreeNode statementNode = new TreeNode("statement", "Statements",
				currentToken.getLine());
		whileNode.add(statementNode);
		if(hasBrace) {
		while (currentToken != null
				&& !currentToken.getContent().equals(ConstVar.RBRACE)) {
			if (!currentToken.getContent().equals(ConstVar.RBRACE))
				statementNode.add(statement());
			else if (statementNode.getChildCount() == 0) {
				whileNode.remove(whileNode.getChildCount() - 1);
				statementNode.setContent("EmptyStm");
				whileNode.add(statementNode);
				break;
			} else {
				break;
			}
		}
		// ƥ���Ҵ�����}
		if (currentToken != null
				&& currentToken.getContent().equals(ConstVar.RBRACE)) {
			nextToken();
		} else { // ����
			String error = " whileѭ��ȱ���Ҵ�����\"}\"" + "\n";
			error(error);
			whileNode.add(new TreeNode(ConstVar.ERROR + "whileѭ��ȱ���Ҵ�����\"}\""));
		}
		} else {
			if(currentToken != null)
				statementNode.add(statement());
		}
		return whileNode;
	}

	/**
	 * read_stm: READ LPAREN ID RPAREN SEMICOLON;
	 * 
	 * @return TreeNode
	 */
	private final TreeNode read_stm() {
		// ����Ҫ���صĽ��
		TreeNode tempNode = null;
		nextToken();
		// ƥ��������(
		if (currentToken != null
				&& currentToken.getContent().equals(ConstVar.LPAREN)) {
			nextToken();
		} else {
			String error = " read���ȱ��������\"(\"" + "\n";
			error(error);
			return new TreeNode(ConstVar.ERROR + "read���ȱ��������\"(\"");
		}
		// ƥ���ʶ��
		if (currentToken != null && currentToken.getKind().equals("��ʶ��")) {
			tempNode = new TreeNode("��ʶ��", currentToken.getContent(),
					currentToken.getLine());
			nextToken();
			// �ж��Ƿ���Ϊ���鸳ֵ
			if (currentToken != null
					&& currentToken.getContent().equals(ConstVar.LBRACKET)) {
				tempNode.add(array());
			}
		} else {
			String error = " read��������ź��Ǳ�ʶ��" + "\n";
			error(error);
			nextToken();
			return new TreeNode(ConstVar.ERROR + "read��������ź��Ǳ�ʶ��");
		}
		// ƥ��������)
		if (currentToken != null
				&& currentToken.getContent().equals(ConstVar.RPAREN)) {
			nextToken();
		} else {
			String error = " read���ȱ��������\")\"" + "\n";
			error(error);
			return new TreeNode(ConstVar.ERROR + "read���ȱ��������\")\"");
		}
		// ƥ��ֺ�;
		if (currentToken != null
				&& currentToken.getContent().equals(ConstVar.SEMICOLON)) {
			nextToken();
		} else {
			String error = " read���ȱ�ٷֺ�\";\"" + "\n";
			error(error);
			return new TreeNode(ConstVar.ERROR + "read���ȱ�ٷֺ�\";\"");
		}
		return tempNode;
	}

	/**
	 * write_stm: WRITE LPAREN expression RPAREN SEMICOLON;
	 * 
	 * @return TreeNode
	 */
	private final TreeNode write_stm() {
		// ����Ҫ���صĽ��
		TreeNode tempNode = null;
		nextToken();
		// ƥ��������(
		if (currentToken != null
				&& currentToken.getContent().equals(ConstVar.LPAREN)) {
			nextToken();
		} else {
			String error = " write���ȱ��������\"(\"" + "\n";
			error(error);
			return new TreeNode(ConstVar.ERROR + "write���ȱ��������\"(\"");
		}
		// ����expression����ƥ����ʽ
		tempNode = expression();
		// ƥ��������)
		if (currentToken != null
				&& currentToken.getContent().equals(ConstVar.RPAREN)) {
			nextToken();
		} else {
			String error = " write���ȱ��������\")\"" + "\n";
			error(error);
			return new TreeNode(ConstVar.ERROR + "write���ȱ��������\")\"");
		}
		// ƥ��ֺ�;
		if (currentToken != null
				&& currentToken.getContent().equals(ConstVar.SEMICOLON)) {
			nextToken();
		} else {
			String error = " write���ȱ�ٷֺ�\";\"" + "\n";
			error(error);
			return new TreeNode(ConstVar.ERROR + "write���ȱ�ٷֺ�\";\"");
		}
		return tempNode;
	}

	/**
	 * assign_stm: (ID | ID array) ASSIGN expression SEMICOLON;
	 * 
	 * @param isFor
	 *            �Ƿ�����forѭ���е���
	 * @return TreeNode
	 */
	private final TreeNode assign_stm(boolean isFor) {
		// assign�������ؽ��ĸ����
		TreeNode assignNode = new TreeNode("�����", ConstVar.ASSIGN, currentToken
				.getLine());
		TreeNode idNode = new TreeNode("��ʶ��", currentToken.getContent(),
				currentToken.getLine());
		assignNode.add(idNode);
		nextToken();
		// �ж��Ƿ���Ϊ���鸳ֵ
		if (currentToken != null
				&& currentToken.getContent().equals(ConstVar.LBRACKET)) {
			idNode.add(array());
		}
		// ƥ�丳ֵ����=
		if (currentToken != null
				&& currentToken.getContent().equals(ConstVar.ASSIGN)) {
			nextToken();
		} else { // ����
			String error = " ��ֵ���ȱ��\"=\"" + "\n";
			error(error);
			return new TreeNode(ConstVar.ERROR + "��ֵ���ȱ��\"=\"");
		}
		// expression
		assignNode.add(condition());
		// ���������forѭ������е����������,��ƥ��ֺ�
		if (!isFor) {
			// ƥ��ֺ�;
			if (currentToken != null
					&& currentToken.getContent().equals(ConstVar.SEMICOLON)) {
				nextToken();
			} else { // ����
				String error = " ��ֵ���ȱ�ٷֺ�\";\"" + "\n";
				error(error);
				assignNode.add(new TreeNode(ConstVar.ERROR + "��ֵ���ȱ�ٷֺ�\";\""));
			}
		}
		return assignNode;
	}

	/**
	 * declare_stm: (INT | REAL | BOOL | STRING) declare_aid(COMMA declare_aid)*
	 * SEMICOLON;
	 * 
	 * @return TreeNode
	 */
	private final TreeNode declare_stm() {
		TreeNode declareNode = new TreeNode("�ؼ���", currentToken.getContent(),
				currentToken.getLine());
		nextToken();
		// declare_aid
		declareNode = declare_aid(declareNode);
		// ����ͬʱ����������������
		String next = null;
		while (currentToken != null) {
			next = currentToken.getContent();
			if (next.equals(ConstVar.COMMA)) {
				nextToken();
				declareNode = declare_aid(declareNode);
			} else {
				break;
			}
			if (currentToken != null)
				next = currentToken.getContent();
		}
		// ƥ��ֺ�;
		if (currentToken != null
				&& currentToken.getContent().equals(ConstVar.SEMICOLON)) {
			nextToken();
		} else { // ����
			String error = " �������ȱ�ٷֺ�\";\"" + "\n";
			error(error);
			declareNode.add(new TreeNode(ConstVar.ERROR + "�������ȱ�ٷֺ�\";\""));
		}
		return declareNode;
	}

	/**
	 * declare_aid: (ID|ID array)(ASSIGN expression)?;
	 * 
	 * @param root
	 *            �����
	 * @return TreeNode
	 */
	private final TreeNode declare_aid(TreeNode root) {
		if (currentToken != null && currentToken.getKind().equals("��ʶ��")) {
			TreeNode idNode = new TreeNode("��ʶ��", currentToken.getContent(),
					currentToken.getLine());
			root.add(idNode);
			nextToken();
			// ����array�����
			if (currentToken != null
					&& currentToken.getContent().equals(ConstVar.LBRACKET)) {
				idNode.add(array());
			} else if (currentToken != null
					&& !currentToken.getContent().equals(ConstVar.ASSIGN)
					&& !currentToken.getContent().equals(ConstVar.SEMICOLON)
					&& !currentToken.getContent().equals(ConstVar.COMMA)) {
				String error = " ����������,��ʶ������ֲ���ȷ��token" + "\n";
				error(error);
				root
						.add(new TreeNode(ConstVar.ERROR
								+ "����������,��ʶ������ֲ���ȷ��token"));
				nextToken();
			}
		} else { // ����
			String error = " ��������б�ʶ������" + "\n";
			error(error);
			root.add(new TreeNode(ConstVar.ERROR + "��������б�ʶ������"));
			nextToken();
		}
		// ƥ�丳ֵ����=
		if (currentToken != null
				&& currentToken.getContent().equals(ConstVar.ASSIGN)) {
			TreeNode assignNode = new TreeNode("�ָ���", ConstVar.ASSIGN,
					currentToken.getLine());
			root.add(assignNode);
			nextToken();
			assignNode.add(condition());
		}
		return root;
	}

	/**
	 * condition: expression (comparison_op expression)? | ID;
	 * 
	 * @return TreeNode
	 */
	private final TreeNode condition() {
		// ��¼expression���ɵĽ��
		TreeNode tempNode = expression();
		// ��������ж�Ϊ�Ƚϱ��ʽ
		if (currentToken != null
				&& (currentToken.getContent().equals(ConstVar.EQUAL)
						|| currentToken.getContent().equals(ConstVar.NEQUAL)
						|| currentToken.getContent().equals(ConstVar.LT) || currentToken
						.getContent().equals(ConstVar.GT))) {
			TreeNode comparisonNode = comparison_op();
			comparisonNode.add(tempNode);
			comparisonNode.add(expression());
			return comparisonNode;
		}
		// ��������ж�Ϊbool����
		return tempNode;
	}

	/**
	 * expression: term (add_op term)?;
	 * 
	 * @return TreeNode
	 */
	private final TreeNode expression() {
		// ��¼term���ɵĽ��
		TreeNode tempNode = term();

		// �����һ��tokenΪ�ӺŻ����
		while (currentToken != null
				&& (currentToken.getContent().equals(ConstVar.PLUS) || currentToken
						.getContent().equals(ConstVar.MINUS))) {
			// add_op
			TreeNode addNode = add_op();
			addNode.add(tempNode);
			tempNode = addNode;
			tempNode.add(term());
		}
		return tempNode;
	}

	/**
	 * term : factor (mul_op factor)?;
	 * 
	 * @return TreeNode
	 */
	private final TreeNode term() {
		// ��¼factor���ɵĽ��
		TreeNode tempNode = factor();

		// �����һ��tokenΪ�˺Ż����
		while (currentToken != null
				&& (currentToken.getContent().equals(ConstVar.TIMES) || currentToken
						.getContent().equals(ConstVar.DIVIDE))) {
			// mul_op
			TreeNode mulNode = mul_op();
			mulNode.add(tempNode);
			tempNode = mulNode;
			tempNode.add(factor());
		}
		return tempNode;
	}

	/**
	 * factor : TRUE | FALSE | REAL_LITERAL | INTEGER_LITERAL | ID | LPAREN
	 * expression RPAREN | DQ string DQ | ID array;
	 * 
	 * @return TreeNode
	 */
	private final TreeNode factor() {
		// ����Ҫ���صĽ��
		TreeNode tempNode = null;
		if (currentToken != null && currentToken.getKind().equals("����")) {
			tempNode = new TreeNode("����", currentToken.getContent(),
					currentToken.getLine());
			nextToken();
		} else if (currentToken != null && currentToken.getKind().equals("ʵ��")) {
			tempNode = new TreeNode("ʵ��", currentToken.getContent(),
					currentToken.getLine());
			nextToken();
		} else if (currentToken != null
				&& currentToken.getContent().equals(ConstVar.TRUE)) {
			tempNode = new TreeNode("����ֵ", currentToken.getContent(),
					currentToken.getLine());
			nextToken();
		} else if (currentToken != null
				&& currentToken.getContent().equals(ConstVar.FALSE)) {
			tempNode = new TreeNode("����ֵ", currentToken.getContent(),
					currentToken.getLine());
			nextToken();
		} else if (currentToken != null && currentToken.getKind().equals("��ʶ��")) {
			tempNode = new TreeNode("��ʶ��", currentToken.getContent(),
					currentToken.getLine());
			nextToken();
			// array
			if (currentToken != null
					&& currentToken.getContent().equals(ConstVar.LBRACKET)) {
				tempNode.add(array());
			}
		} else if (currentToken != null
				&& currentToken.getContent().equals(ConstVar.LPAREN)) { // ƥ��������(
			nextToken();
			tempNode = expression();
			// ƥ��������)
			if (currentToken != null
					&& currentToken.getContent().equals(ConstVar.RPAREN)) {
				nextToken();
			} else { // ����
				String error = " ��ʽ����ȱ��������\")\"" + "\n";
				error(error);
				return new TreeNode(ConstVar.ERROR + "��ʽ����ȱ��������\")\"");
			}
		} else if (currentToken != null
				&& currentToken.getContent().equals(ConstVar.DQ)) { // ƥ��˫����
			nextToken();
			tempNode = new TreeNode("�ַ���", currentToken.getContent(),
					currentToken.getLine());
			nextToken();
			// ƥ������һ��˫����
			nextToken();
		} else { // ����
			String error = " ��ʽ���Ӵ��ڴ���" + "\n";
			error(error);
			if (currentToken != null
					&& !currentToken.getContent().equals(ConstVar.SEMICOLON)) {
				nextToken();
			}
			return new TreeNode(ConstVar.ERROR + "��ʽ���Ӵ��ڴ���");
		}
		return tempNode;
	}

	/**
	 * array : LBRACKET (expression) RBRACKET;
	 * 
	 * @return TreeNode
	 */
	private final TreeNode array() {
		// ����Ҫ���صĽ��
		TreeNode tempNode = null;
		if (currentToken != null
				&& currentToken.getContent().equals(ConstVar.LBRACKET)) {
			nextToken();
		} else {
			String error = " ȱ����������\"[\"" + "\n";
			error(error);
			return new TreeNode(ConstVar.ERROR + "ȱ����������\"[\"");
		}
		// ����expression����ƥ����ʽ
		tempNode = expression();
		if (currentToken != null
				&& currentToken.getContent().equals(ConstVar.RBRACKET)) {
			nextToken();
		} else { // ����
			String error = " ȱ����������\"]\"" + "\n";
			error(error);
			return new TreeNode(ConstVar.ERROR + "ȱ����������\"]\"");
		}
		return tempNode;
	}

	/**
	 * add_op : PLUS | MINUS;
	 * 
	 * @return TreeNode
	 */
	private final TreeNode add_op() {
		// ����Ҫ���صĽ��
		TreeNode tempNode = null;
		if (currentToken != null
				&& currentToken.getContent().equals(ConstVar.PLUS)) {
			tempNode = new TreeNode("�����", ConstVar.PLUS, currentToken
					.getLine());
			nextToken();
		} else if (currentToken != null
				&& currentToken.getContent().equals(ConstVar.MINUS)) {
			tempNode = new TreeNode("�����", ConstVar.MINUS, currentToken
					.getLine());
			nextToken();
		} else { // ����
			String error = " �Ӽ����ų���" + "\n";
			error(error);
			return new TreeNode(ConstVar.ERROR + "�Ӽ����ų���");
		}
		return tempNode;
	}

	/**
	 * mul_op : TIMES | DIVIDE;
	 * 
	 * @return TreeNode
	 */
	private final TreeNode mul_op() {
		// ����Ҫ���صĽ��
		TreeNode tempNode = null;
		if (currentToken != null
				&& currentToken.getContent().equals(ConstVar.TIMES)) {
			tempNode = new TreeNode("�����", ConstVar.TIMES, currentToken
					.getLine());
			nextToken();
		} else if (currentToken != null
				&& currentToken.getContent().equals(ConstVar.DIVIDE)) {
			tempNode = new TreeNode("�����", ConstVar.DIVIDE, currentToken
					.getLine());
			nextToken();
		} else { // ����
			String error = " �˳����ų���" + "\n";
			error(error);
			return new TreeNode(ConstVar.ERROR + "�˳����ų���");
		}
		return tempNode;
	}

	/**
	 * comparison_op: LT | GT | EQUAL | NEQUAL;
	 * 
	 * @return TreeNode
	 */
	private final TreeNode comparison_op() {
		// ����Ҫ���صĽ��
		TreeNode tempNode = null;
		if (currentToken != null
				&& currentToken.getContent().equals(ConstVar.LT)) {
			tempNode = new TreeNode("�����", ConstVar.LT, currentToken.getLine());
			nextToken();
		} else if (currentToken != null
				&& currentToken.getContent().equals(ConstVar.GT)) {
			tempNode = new TreeNode("�����", ConstVar.GT, currentToken.getLine());
			nextToken();
		} else if (currentToken != null
				&& currentToken.getContent().equals(ConstVar.EQUAL)) {
			tempNode = new TreeNode("�����", ConstVar.EQUAL, currentToken
					.getLine());
			nextToken();
		} else if (currentToken != null
				&& currentToken.getContent().equals(ConstVar.NEQUAL)) {
			tempNode = new TreeNode("�����", ConstVar.NEQUAL, currentToken
					.getLine());
			nextToken();
		} else { // ����
			String error = " �Ƚ����������" + "\n";
			error(error);
			return new TreeNode(ConstVar.ERROR + "�Ƚ����������");
		}
		return tempNode;
	}

}