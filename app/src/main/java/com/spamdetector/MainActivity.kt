package com.spamdetector

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import com.spamdetector.contract.MainContract
import com.spamdetector.databinding.ActivityMainBinding
import com.spamdetector.model.data.ClientInfo
import com.spamdetector.presenter.MainPresenter
import com.spamdetector.utils.AnimationManager.Companion.animateMove
import com.spamdetector.utils.AnimationManager.Companion.animateResize
import com.spamdetector.utils.Preferences
import com.spamdetector.utils.StringManager
import com.spamdetector.utils.UsPhoneNumberFormatter
import com.spamdetector.dialog.LoadingDialog
import java.lang.ref.WeakReference

class MainActivity : BaseActivity(), MainContract.View {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mainPresenter: MainContract.Presenter
    private lateinit var loadingDialog: LoadingDialog

    var searchToggle = 0
    var firstTime = 0L
    var secondTime = 0L

    private var panelHeight = 0
    private var distX = 0F
    private var distY = 0F

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initPresenter()
        initView()
    }

    override fun initPresenter() { mainPresenter = MainPresenter() }
    override fun initView() {
        mainPresenter.takeView(this)
        // 소프트 키 투명화 //
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        binding.apply {
            optionBtn.setOnClickListener {
                mainDrawer.openDrawer(rightDrawer)
            }
            numberInput.addTextChangedListener(
                UsPhoneNumberFormatter(WeakReference(numberInput))
            )
            numberInput.setOnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
                    search(numberInput.text.toString())
                }
                false
            }
            mainDrawer.setOnClickListener {
                val inputMethodManager =
                    getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
            }
            slidingLayout.addPanelSlideListener(PanelEventListener())

            noticeBtn.setOnClickListener {
                startActivity(Intent(applicationContext, NoticeActivity::class.java))
            }
//            blockBtn.setOnClickListener {
//                startActivity(Intent(applicationContext, BlockActivity::class.java))
//            }
//            settingBtn.setOnClickListener {
//                startActivity(Intent(applicationContext, SettingActivity::class.java))
//            }
        }
    }
    override fun initPref() { Preferences.init(applicationContext) }

    override fun showLoading(dialog: LoadingDialog) {
        loadingDialog = dialog
        loadingDialog.show(supportFragmentManager, loadingDialog.tag)
    }
    override fun dismissLoading() {
        loadingDialog.dismiss()
    }

    override fun search(str: String) {
        binding.numberInput.setText(str)
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager // 키보드 내리기
        inputMethodManager.hideSoftInputFromWindow(binding.numberInput.windowToken, 0)

        val phoneNum = StringManager.changeToHyphenNumber(str.replace(")", "").replace("-", ""))
        mainPresenter.doSearch(phoneNum)
    }
    override fun showResult(clientInfo: ClientInfo?) {
        binding.numberInput.apply {animate()
            .scaleX(0.5F)
            .scaleY(0.5F)
            .withEndAction {
                val editLocation = IntArray(2)
                getLocationOnScreen(editLocation)
                val mainLocation = IntArray(2)
                binding.topPoint.getLocationOnScreen(mainLocation)
                distX = -(editLocation[0] - mainLocation[0]).toFloat()
                distY = -(editLocation[1] - mainLocation[1]).toFloat()
                animateMove(this, distX, distY, 400L)

                binding.noResultView.visibility = View.VISIBLE
                binding.mainText.visibility = View.GONE
                binding.slidingLayout.panelHeight = 0
                binding.mainLayout.setBackgroundColor(
                    ContextCompat.getColor(this@MainActivity, R.color.colorGrayBackground)
                )

                isFocusableInTouchMode = false
                isClickable = true
                clearFocus()
                setOnClickListener { backToMain() }

                when (clientInfo) {
                    null -> binding.noResultView.text = "아무것도 찾지 못했어요."
                    else -> binding.noResultView.text = "찾았습니다"
                }
                loadingDialog.dismiss()
            }
            .start()
        }
        searchToggle = 1
    }

    override fun onBackPressed() {
        if (binding.mainDrawer.isDrawerOpen(binding.rightDrawer)) { // 오른쪽 패널이 열려있으면
            binding.mainDrawer.closeDrawer(binding.rightDrawer)
            return
        }
        if (binding.slidingLayout.panelState == SlidingUpPanelLayout.PanelState.EXPANDED) {
            binding.slidingLayout.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
            return
        }
        if (searchToggle == 1) {
            backToMain()
            return
        }

        secondTime = System.currentTimeMillis()
        Toast.makeText(applicationContext, "한번 더 누르면 종료됩니다", Toast.LENGTH_SHORT).show()
        if (secondTime - firstTime < 2000) {
            super.onBackPressed()
            finishAffinity()
        }
        firstTime = System.currentTimeMillis()
    }

    private fun backToMain() {
        binding.apply {
            noResultView.visibility = View.GONE
            mainText.visibility = View.VISIBLE
            numberInfo.visibility = View.INVISIBLE
            slidingLayout.panelHeight = panelHeight
            numberInput.isFocusableInTouchMode =true

            distX = 1 / -(distX / 100)
            distY = 1 / -(distY / 100)
            numberInput.animate()
                .translationX(distX)
                .translationY(distY)
                .withEndAction {
                    animateResize(numberInput, 1.0F, 400L)

                    mainText.text = "전화번호를 입력하세요."
                    mainLayout.setBackgroundColor(Color.WHITE)
                    numberInput.isFocusableInTouchMode = true
                    numberInput.setOnClickListener {}
//                number_input.requestFocus() // 키패드 올리기
//                Selection.setSelection(number_input.text, number_input.length()) // 끝으로 포커스
//                var imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//                imm.toggleSoftInput(
//                    InputMethodManager.SHOW_FORCED,
//                    InputMethodManager.HIDE_IMPLICIT_ONLY
//                )
                }
                .start()

            searchToggle = 0
        }
    }

    inner class PanelEventListener : SlidingUpPanelLayout.PanelSlideListener {
        override fun onPanelSlide(panel: View?, slideOffset: Float) { return }
        override fun onPanelStateChanged(
            panel: View?,
            previousState: SlidingUpPanelLayout.PanelState?,
            newState: SlidingUpPanelLayout.PanelState?
        ) {
            binding.apply {
                if (newState == SlidingUpPanelLayout.PanelState.EXPANDED)  //열릴때
                {
                    expendBtn.setImageResource(R.drawable.down_icon)
                    panelText.visibility = View.VISIBLE
                    recentCallMsg.visibility = View.VISIBLE
                    return
                } else if (newState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                    expendBtn.setImageResource(R.drawable.up_icon)
                    recentCallList.smoothScrollToPosition(0)
                    panelText.visibility = View.INVISIBLE
                    recentCallMsg.visibility = View.INVISIBLE
                }
            }
        }
    }
}