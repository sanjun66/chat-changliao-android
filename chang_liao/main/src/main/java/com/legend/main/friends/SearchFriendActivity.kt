package com.legend.main.friends

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Handler
import android.os.Message
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.legend.baseui.ui.base.BaseActivity
import com.legend.baseui.ui.widget.toast.ToastUtils
import com.legend.common.KeyConst
import com.legend.common.Router
import com.legend.common.RouterPath
import com.legend.common.TypeConst
import com.legend.common.bean.UserBean
import com.legend.main.R
import com.legend.main.databinding.ActivitySearchFriendsBinding
import com.legend.main.network.viewmodel.FriendViewModel
import java.lang.ref.WeakReference

@Route(path = RouterPath.path_search_friend_activity)
class SearchFriendActivity: BaseActivity<ActivitySearchFriendsBinding>() {
    private val REQUEST_CODE_ADD_FRIEND = 100
    private var searchAdapter: SearchFriendAdapter? = null
    private val handler = MyHandler(WeakReference(this))
    private val friendList = mutableListOf<UserBean.SearchFriend>()

    private val viewModel: FriendViewModel by lazy {
      ViewModelProvider(this)[FriendViewModel::class.java]
    }

    override fun getLayoutId(): Int = R.layout.activity_search_friends

    override fun initView() {
        mDataBinding?.apply {
            recyclerSearchResult.layoutManager = LinearLayoutManager(mContext)
            searchAdapter = SearchFriendAdapter()
            recyclerSearchResult.adapter = searchAdapter
            recyclerSearchResult.addItemDecoration(DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL))
            searchAdapter?.setOnItemClickListener { adapter, view, position ->
                val item: UserBean.SearchFriend = adapter.getItem(position) as UserBean.SearchFriend
                Router.toChatActivity("s" + item.id, item.nick_name)
                finish()
            }
            searchAdapter?.addFriendListner = object : AddFriendListener {
                override fun onAddFriend(id: String) {
                    Router.toInputTransActivity(this@SearchFriendActivity, REQUEST_CODE_ADD_FRIEND, TypeConst.trans_input_page_type_add_friend, id)
                }
            }

            edtText.setOnEditorActionListener(object: OnEditorActionListener{
                override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                    if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                        search()
                        return true
                    }
                    return false
                }
            })

            // 边输入边搜索
//            editSearch()
            imgClear.setOnClickListener {
                edtText.setText("")
                imgClear.visibility = View.INVISIBLE
                showSearchResult(false)
            }

            tvCancel.setOnClickListener { finish() }
        }
    }

    private fun editSearch() {
        mDataBinding?.edtText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?,start: Int,count: Int,after: Int) {

            }

            @SuppressLint("NotifyDataSetChanged")
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                handler.removeCallbacksAndMessages(null)

                if (!TextUtils.isEmpty(s)) {
                    val msg = Message.obtain()
                    msg.what = 100
                    msg.obj = s
                    handler.sendMessageDelayed(msg, 100)
                    mDataBinding?.imgClear?.visibility = View.VISIBLE
                } else {
                    friendList.clear()
                    searchAdapter?.notifyDataSetChanged()
                    showSearchResult(false)
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }

        })
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun initData() {
        viewModel.searchFriendRes.observe(this) {
            if (it.friend_list.isEmpty()) {
                ToastUtils.show(getString(R.string.user_not_exit))
            } else {
                friendList.clear()
                for (friend in it.friend_list) {
                    friendList.add(friend)
                }
                searchAdapter?.data = friendList
                searchAdapter?.notifyDataSetChanged()
            }
        }

        viewModel.applyFriendSuccess.observe(this) {
            if (it.isSuccess) {
                ToastUtils.show(getString(R.string.apply_friend_success))
                finish()
            } else {
                ToastUtils.show(it.message)
            }
        }
    }

    private fun showSearchResult(b: Boolean) {

    }

    private fun search() {
        if (TextUtils.isEmpty(mDataBinding?.edtText?.text?.toString()?.trim())) {
            ToastUtils.show(getString(R.string.input_account))
            return
        }
        handler.removeCallbacksAndMessages(null)
        viewModel.searchFriend(mDataBinding?.edtText?.text?.toString()?.trim()!!)
    }

    class MyHandler(private val weakActivity: WeakReference<SearchFriendActivity>) : Handler() {

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (weakActivity.get() == null) return
            if (msg.what == 100) {
                weakActivity.get()!!.search()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when(requestCode) {
                REQUEST_CODE_ADD_FRIEND -> {
                    val id = data?.getStringExtra(KeyConst.key_id)?:""
                    val inputReason = data?.getStringExtra(KeyConst.key_input_content)?:""
                    viewModel.applyFriend(id, inputReason)
                }
            }
        }
    }

}
