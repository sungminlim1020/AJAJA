package com.example.ajaja

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment

class NoticeFragment : Fragment(R.layout.fragment_notice) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<CardView>(R.id.cardNotice).setOnClickListener {
            // TODO: NoticeListActivity/Fragment 연결
            Toast.makeText(requireContext(), "공지사항으로 이동 (구현 예정)", Toast.LENGTH_SHORT).show()
        }
        view.findViewById<CardView>(R.id.cardNotification).setOnClickListener {
            // TODO: NotificationHistoryActivity 연결
            Toast.makeText(requireContext(), "최근 알림으로 이동 (구현 예정)", Toast.LENGTH_SHORT).show()
        }
        view.findViewById<CardView>(R.id.cardMessage).setOnClickListener {
            // TODO: MessageListActivity 연결
            Toast.makeText(requireContext(), "개별 메시지로 이동 (구현 예정)", Toast.LENGTH_SHORT).show()
        }
    }
}
