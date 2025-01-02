//###################################################################
// プレイヤーのスーパークラス（抽象クラス）
abstract class MsPlayer {
	// プレイヤーの数
	private static int numPlayers = 0;

	protected final int    id;       // ID
	protected final String name;     // 名前 (クラス名から自動取得)
	protected MsBoard board;

	//***************************************************************
	// コンストラクタ
	public MsPlayer(){
		id    = numPlayers;
		numPlayers++;
		name  = id + "/" + this.getClass().getSimpleName();
	}

	public MsPlayer(MsBoard board){
		this();
		this.board = board;
	}

	//***************************************************************
	// 行動する (抽象メソッド:サブクラスで必ず実装する)
	abstract public void play();

	// プレイヤーの状態をリセットする
	public void reset(MsBoard board) {
		this.board = board;
	}

	//***************************************************************
	// アクセッサ
	public String getName()        { return name; }
}
