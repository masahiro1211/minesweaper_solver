
import java.util.ArrayList;
import java.util.Scanner;

//###################################################################
// MsPlayerの状態を記録するクラス
/*
 * MsPlayer内部で書き換えられないようにする
 */
class MsPlayerRecord {
	// 持ち時間[ms]
	private static final long allottedTime = 120000; // 2 min

	private MsPlayer  player;        // プレイヤー
	private int       score;         // スコア
	private long      remainingTime; // 残り時間[ms]
	protected boolean isPlayable;    // 反則負けになっていないか

	//***************************************************************
	// コンストラクタ
	MsPlayerRecord(MsPlayer player) {
		this.player = player;
		this.score  = 0;
		this.remainingTime = allottedTime;
		this.isPlayable = true;
	}

	//***************************************************************
	// スコアを加算する
	public void addScore(int score) {
		this.score += score;
	}

	// 残り時間を減らす
	public void subtractTime(long time)
	{
		this.remainingTime -= time;
	}

	//***************************************************************
	// 状態を表示する
	public String getStatus() {
		return player.getName()
			+ (isPlayable?
				(" : score " + score
					+ " ("
					+ (remainingTime/1000.0)
					+ " s remaining)"):
				(" : LOST"));
	}

	//***************************************************************
	// アクセッサ
	MsPlayer getPlayer()    { return player; }
	int getScore()          { return score; }
	long getRemainingTime() { return remainingTime; }
	boolean isPlayable()    { return isPlayable; }
	void setPlayable()      { isPlayable = true; }
	void unsetPlayable()    { isPlayable = false; }
}

//###################################################################
// ゲームの進行を管理するクラス
class MsGame {
	Scanner stdIn = new Scanner(System.in);

	// 盤面
	private MsBoard  board;
	// プレイヤーのリスト (可変長配列ArrayListで保持)
	private ArrayList<MsPlayerRecord> records
	= new ArrayList<MsPlayerRecord>();

	// ゲーム中であるかどうかをあらわすフラグ
	private boolean isStarted = false;
	// 残り時間制限が有効であるかどうかをあらわすフラグ
	private boolean isClockEnabled = false;
	// ゲームの経過を表示するかどうかをあらわすフラグ
	private boolean isMsgShown = true;
	// デバッグ用のフラグ
	boolean isDebugMode = false;

	//***************************************************************
	// コンストラクタ
	public MsGame() {}

	public MsGame(MsBoard board) {
		this();
		this.board = board;
	}

	//***************************************************************
	// プレイヤーを追加する
	public void addPlayer(MsPlayer player) {
		if (isStarted == false) {
			MsPlayerRecord record = new MsPlayerRecord(player);
			records.add(record);
		}
		else {
			System.out.println
			("ERROR: players cannot be added after game started.");
		}
	}

	//***************************************************************
	// ゲームを進行する
	public MsPlayer start() {
		// プレイヤーが1人以上登録されているかチェックする
		if (records.size()<1) {
			System.out.println("ERROR: no players added.");
			return null;
		}

		isStarted = true;
		int turn  = 0;
		while (true) {
			//-------------------------------------------------------
			// ターンの準備
			resetTurn();

			//-------------------------------------------------------
			// そのターンでプレイするプレーヤーの決定と表示
			MsPlayerRecord record = records.get(turn%records.size());
			MsPlayer player = record.getPlayer();
			if (isMsgShown) {
				System.out.println();
				System.out.println
				("Turn " + turn + ": " + player.getName());
			}
			if (!record.isPlayable())
			{
				System.out.println("- skipped.");
				turn++;
				continue;
			}

			//-------------------------------------------------------
			// プレイヤーの行動
			int loopCounter = 0;
			long startTime = System.currentTimeMillis();
			while (true) {
				player.play();

				// 不正操作の判定
				if (board.getNumPlayed() >= 2 ||
						(board.getNumPlayed() == 1
							&& board.isValidPlay() == false)) {
					System.out.println("OOPS! VIOLATION DETECTED!");
					System.out.println(player.getName() + " lost...");
					record.unsetPlayable();
					break;
				}
				else if (board.isValidPlay() == false) {
					System.out.println("- play again.");
					record.addScore(-100);
					loopCounter++;
				}
				else {
					break;
				}

				// 間違えすぎたら反則負け
				if (loopCounter >= 5) {
					System.out.println("OOPS! TOO MANY FAULT!");
					System.out.println(player.getName() + " lost...");
					record.unsetPlayable();
					break;
				}
			}

			//-------------------------------------------------------
			// 得点と残り時間の処理
			record.addScore(board.getLastPlayScore());
			if (isClockEnabled)
			{
				long endTime = System.currentTimeMillis();
				record.subtractTime(endTime - startTime);
			}

			// プレイヤー名，得点，残り時間の表示
			if (isMsgShown) {
				for (int i=0; i<records.size(); i++) {
					System.out.println
					("- " + records.get(i).getStatus());
				}
			}

			// 残り時間が無くなったら反則負け
			if (record.getRemainingTime()<0) {
				System.out.println("OOPS! TIME IS UP!");
				System.out.println(player.getName() + " lost...");
				record.unsetPlayable();
			}

			//-------------------------------------------------------
			// 終了判定
			boolean isGameOver = true;
			int numPlayable = 0;
			for (int i=0; i<records.size(); i++)
			{
				if (records.get(i).isPlayable())
				{
					numPlayable++;
				}
				if (numPlayable>=2)
				{
					isGameOver = false;
					break;
				}
			}
			if (!isGameOver)
			{
				isGameOver = board.isGameOver();
			}
			if (isGameOver==true) {
				System.out.println("\nFinished.");
				break;
			}

			if (isDebugMode==true) {
				System.out.print("Press Enter....");
				stdIn.nextLine();
			}

			//-------------------------------------------------------
			// 次のターンへ
			turn++;
		}

		for (int i=0; i<records.size(); i++) {
			System.out.println
			("- " + records.get(i).getStatus());
		}
		isStarted = false;
		return getWinner();
	}

	//***************************************************************
	// ターンの準備をする
	private void resetTurn()
	{
		board.resetStatus();
	}

	//***************************************************************
	// 勝者を返す
	public MsPlayer getWinner() {
		MsPlayerRecord winnerRecord = null;
		for (int i=0; i<records.size(); i++) {
			MsPlayerRecord record = records.get(i);

			// 反則負けしたプレイヤーは除外
			if (!record.isPlayable())
			{
				continue;
			}
			else if (winnerRecord == null)
			{
				winnerRecord = record;
				continue;
			}
			// 得点の高い者が勝ち
			if (record.getScore()>winnerRecord.getScore()) {
				winnerRecord = record;
			}
			// 同点の場合は残り時間の多い者が勝ち
			else if (record.getScore()==winnerRecord.getScore()
					&& record.getRemainingTime()
					> winnerRecord.getRemainingTime()){
				winnerRecord = record;
			}
			// それでも同点の場合はIDの若い者が勝ち
		}
		return winnerRecord.getPlayer();
	}

	//***************************************************************
	// アクセッサ
	public boolean isMsgShown()     { return isMsgShown; }
	public boolean isClockEnabled() { return isClockEnabled; }
	public boolean isDebugMode()    { return isDebugMode; }

	public void setMsgShown(boolean flag) {
		isMsgShown = flag;
		board.setMsgShown(flag);
	}

	public void setClockEnabled(boolean flag) {
		isClockEnabled = flag;
	}

	public void setDebugMode(boolean flag) {
		isDebugMode = flag;
	}
}
