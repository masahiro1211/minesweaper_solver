
//###################################################################
// 対戦型マインスイーパを開始するクラス
public class MsBattle {
	public static void main(String[] args) {
		// ゲームのレベル
		// 1: 初級 (9x9, 10 bombs)
		// 2: 中級 (16x16, 40 bombs)
		// 3: 上級 (30x16, 99 bombs)
		int level = 3;

		// 盤面の生成
		MsBoard board = new MsBoard(level);

		// 引き続き下のような盤面の生成も可能
		// MsBoard board = new MsBoard(9, 9, 10);

		// プレイヤーの生成
		MsPlayer player1 = new MsMasahiroPlayer(board);
		MsPlayer player2 = new MsMasahiroPlayer_stable(board);

		//-------------------------------------------------------
		// ゲームの生成
		MsGame game = new MsGame(board);

		// 残り時間制限を有効にする場合には下を呼び出す．
		game.setClockEnabled(true);

		// 各ターンの出力を抑制する場合は下を呼び出す．
		// game.setMsgShown(false);

		// 各ターンで止めEnterキー入力を促す場合は下を呼び出す．
		game.setDebugMode(true);

		// ゲームへのプレイヤーの登録
		game.addPlayer(player1);
		game.addPlayer(player2);

		//-------------------------------------------------------
		// ゲームのスタート (勝者を返す)
		MsPlayer winner = game.start();

		if (winner!=null) {
			System.out.println("\nWinner : " + winner.getName());
		}
	}
}

