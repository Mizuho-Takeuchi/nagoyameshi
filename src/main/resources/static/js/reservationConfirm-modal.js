console.log("★JSファイル自体は無事に読み込まれています！");

// 画面のHTMLがすべて読み込まれてから実行する安全装置
document.addEventListener('DOMContentLoaded', () => {

	// 仮予約承認用のフォームを取得
	const approveReservationForm = document.forms.approveReservationForm;

	// フォームが存在するときだけ処理を実行（エラー防止）
	if (approveReservationForm) {
		// Thymeleafが書き換えたベースのURL（/manager/approval）を最初に1回だけ記憶
		const baseApproveAction = approveReservationForm.getAttribute('action');

		// 仮予約承認用のモーダルが開いたときの処理
		const approveModalElement = document.getElementById('approveReservationModal');
		if (approveModalElement) {
			approveModalElement.addEventListener('show.bs.modal', (event) => {
				// クリックされた「承認」リンク（aタグ）を取得
				let approveButton = event.relatedTarget;

				// aタグの th:data-reservation-id から予約IDを取得
				let reservationId = approveButton.dataset.reservationId;

				// ベースURLの末尾に「/予約ID」を結合して、送信先を上書き
				approveReservationForm.action = `${baseApproveAction}/${reservationId}`;

				// 確認用ログ
				console.log("★JSが正常に動作しました！送信先URL:", approveReservationForm.action);
			});
		}
	}

});