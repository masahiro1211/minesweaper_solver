import java.util.Scanner;

//###################################################################
// 人間が操作するプレイヤー
class MsHumanPlayer extends MsPlayer {
	Scanner stdIn = new Scanner(System.in);

	//***************************************************************
	// コンストラクタ
	public MsHumanPlayer(){ super(); }
	public MsHumanPlayer(MsBoard board){ super(board); }

	//***************************************************************
	// 行動する
	public void play() {
		// コマンドを入力する
		System.out.print(" [o]pen cell / put [f]lag? : ");
		char command = stdIn.next().charAt(0);
		if (command!='o' && command!='f') {
			System.out.println("- invalid command.");
			return;
		}

		// オープンするマスの座標を入力する
		System.out.print(" x? : ");
		int x = stdIn.nextInt();
		System.out.print(" y? : ");
		int y = stdIn.nextInt();

		if (command=='o') {
			// マスのオープン
			board.openCell(x, y);
		}
		else if (command=='f') {
			// フラグを立てる
			board.putFlag(x, y);
		}
		else {
			System.out.println("- invalid command.");
		}
	}
}