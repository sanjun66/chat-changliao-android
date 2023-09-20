package com.legend.main.login

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Message
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.legend.baseui.ui.base.ActivityPageConfig
import com.legend.baseui.ui.base.BaseActivity
import com.legend.baseui.ui.util.DisplayUtils
import com.legend.baseui.ui.widget.susindexbar.suspension.SuspensionDecoration
import com.legend.baseui.ui.widget.toast.ToastUtils
import com.legend.common.KeyConst
import com.legend.common.RouterPath
import com.legend.common.bean.InternationAreaCode
import com.legend.main.R
import com.legend.main.databinding.ActivityChooseCountryCodeBinding
import com.legend.main.home.adapter.AreaCodeAdapter
import com.legend.main.network.viewmodel.LoginViewModel
import java.lang.ref.WeakReference

@Route(path = RouterPath.path_choose_country_code_activity)
class ChooseCountryCodeActivity: BaseActivity<ActivityChooseCountryCodeBinding>() {

    private var areaCodeAllAdapter: AreaCodeAdapter? = null
    private var areaCodeAdapter: AreaCodeAdapter? = null
    private var suspensionDecoration: SuspensionDecoration? = null
    private val areaCodeAllList = mutableListOf<InternationAreaCode>()
    private val areaCodeList = mutableListOf<InternationAreaCode>()
    private val mHandler = MyHandler(WeakReference(this))

    private val viewModel: LoginViewModel by lazy {
        ViewModelProvider(this)[LoginViewModel::class.java]
    }

    override fun getLayoutId() = R.layout.activity_choose_country_code

    override fun initView() {
        mDataBinding?.apply {
            val layoutManager = LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false)
            recyclerView.layoutManager = layoutManager
            areaCodeAllAdapter = AreaCodeAdapter()
            recyclerView.adapter = areaCodeAllAdapter
            suspensionDecoration = SuspensionDecoration(mContext, areaCodeAllList)
            suspensionDecoration?.setTitleMarginLeft(DisplayUtils.dp2px(mContext, 25f))
            recyclerView.addItemDecoration(suspensionDecoration!!)
            recyclerView.addItemDecoration(DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL))
            indexBar.setNeedRealIndex(true) // 设置需要真实的索引
                .setmLayoutManager(layoutManager)   // 设置recyclerview的layooutManager
//                .setmPressedShowTextView(tvSideBarHint) // 设置HintTextView

            recyclerSearchResult.layoutManager = LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false)
            areaCodeAdapter = AreaCodeAdapter()
            recyclerSearchResult.adapter = areaCodeAdapter
            recyclerSearchResult.addItemDecoration(DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL))

            areaCodeAdapter?.setOnItemClickListener { adapter, view, position ->
                itemCLicked(adapter.getItem(position) as InternationAreaCode)
            }
            areaCodeAllAdapter?.setOnItemClickListener { adapter, view, position ->
                itemCLicked(adapter.getItem(position) as InternationAreaCode)
            }


            edtCode.setOnEditorActionListener(object : OnEditorActionListener {
                override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                    if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                        search()
                        return true
                    }
                    return false
                }
            })
            edtCode.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                @SuppressLint("NotifyDataSetChanged")
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    mHandler.removeCallbacksAndMessages(null)

                    if (!TextUtils.isEmpty(s)) {
                        val msg = Message.obtain()
                        msg.what = 100
                        msg.obj = s
                        mHandler.sendMessageDelayed(msg, 500)
                        mDataBinding?.imgClear?.visibility = View.VISIBLE
                    } else {
                        areaCodeList.clear()
                        areaCodeAdapter?.notifyDataSetChanged()
                        showSearchResult(false)
                    }
                }

                override fun afterTextChanged(s: Editable?) {
                }
            })

            imgClear.setOnClickListener {
                edtCode.setText("")
                imgClear.visibility = View.INVISIBLE
                showSearchResult(false)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun initData() {
//        viewModel.getAllInternalAreaCodes()

        viewModel.internalAllAreaCodes.observe(this) {
            areaCodeAllList.clear()
            for (item in it) {
                for (area in item.lists) {
                    areaCodeAllList.add(area)
                }
            }
            areaCodeAllAdapter?.data = areaCodeAllList
            areaCodeAllAdapter?.notifyDataSetChanged()
            mDataBinding?.indexBar?.setmSourceDatas(areaCodeAllList)?.invalidate()
            suspensionDecoration?.setmDatas(areaCodeAllList)

            fixFirstNotShowIndex()
        }
        viewModel.internalAreaCodes.observe(this) {
            areaCodeList.clear()
            for (area in it) {
                areaCodeList.add(area)
            }
            areaCodeAdapter?.data = areaCodeList
            areaCodeAdapter?.notifyDataSetChanged()
            showSearchResult(true)
        }
    }

    private fun itemCLicked(item: InternationAreaCode) {
        hideSoftKeyboard()
        val intent = Intent()
        intent.putExtra(KeyConst.key_code_result, item.tel)
        setResult(RESULT_OK, intent)
        finish()
    }

    // 修复不显示index
    private fun fixFirstNotShowIndex() {
        mDataBinding?.edtCode?.requestFocus()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(mDataBinding?.edtCode, 0)
        val msg = Message.obtain()
        msg.what = 101
        mHandler.sendMessageDelayed(msg, 200)
    }

    private fun showSearchResult(isShow: Boolean) {
        if (isShow) {
            mDataBinding?.recyclerView?.visibility = View.INVISIBLE
            mDataBinding?.indexBar?.visibility = View.INVISIBLE
            mDataBinding?.recyclerSearchResult?.visibility = View.VISIBLE
        } else {
            mDataBinding?.recyclerView?.visibility = View.VISIBLE
            mDataBinding?.indexBar?.visibility = View.VISIBLE
            mDataBinding?.recyclerSearchResult?.visibility = View.INVISIBLE
        }
    }

    private fun search() {
        if (TextUtils.isEmpty(mDataBinding?.edtCode?.text?.toString()?.trim())) {
            ToastUtils.show(getString(R.string.input_content))
            return
        }
        mHandler.removeCallbacksAndMessages(null)
//        viewModel.getInternalAreaCodes(mDataBinding?.edtCode?.text?.toString()?.trim())
        mDataBinding?.recyclerSearchResult?.visibility = View.VISIBLE
    }

    override fun initPageConfig(pageConfig: ActivityPageConfig?) {
        super.initPageConfig(pageConfig)
        pageConfig?.showAppTitleBar = true
    }

    override fun getPageTitle(): String {
        return getString(R.string.phone_area)
    }

    private fun hideSoftKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(mDataBinding?.edtCode?.windowToken, 0)
    }

    class MyHandler(private val weakActivity: WeakReference<ChooseCountryCodeActivity>) : Handler() {

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (weakActivity.get() == null) return
            if (msg.what == 100) {
                weakActivity.get()!!.search()
            } else if(msg.what == 101) {
                weakActivity.get()!!.hideSoftKeyboard()
            }
        }
    }
}