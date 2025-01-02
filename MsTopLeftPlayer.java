//###################################################################
// 左上から順にマスを開けていく単純なプレイヤー
class MsTopLeftPlayer extends MsPlayer {
	//***************************************************************
	// コンストラクタ
	public MsTopLeftPlayer(){ super(); }
	public MsTopLeftPlayer(MsBoard board){ super(board); }

	//***************************************************************
	// 行動する
	public void play() {
		int x, y;

		// 左上から順にオープンされていないマスを開く
		for (int i=0; /* 無限ループ条件 */ ; i++) {
			x = i % board.getWidth();
			y = i / board.getWidth();
			if (board.getCell(x, y) == -1) {
				break;
			}
		}

		board.openCell(x, y);
	}
}
