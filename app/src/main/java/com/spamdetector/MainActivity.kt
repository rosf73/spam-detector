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
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import com.spamdetector.contract.MainContract
import com.spamdetector.databinding.ActivityMainBinding
import com.spamdetector.presenter.MainPresenter
import com.spamdetector.utils.AnimationManager.Companion.animateResize
import com.spamdetector.utils.Preferences
import com.spamdetector.utils.StringManager
import com.spamdetector.utils.UsPhoneNumberFormatter
import java.lang.ref.WeakReference

class MainActivity : BaseActivity(), MainContract.View {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mainPresenter: MainContract.Presenter

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

//            noticeBtn.setOnClickListener {
//                startActivity(Intent(applicationContext, NoticeActivity::class.java))
//            }
//            blockBtn.setOnClickListener {
//                startActivity(Intent(applicationContext, BlockActivity::class.java))
//            }
//            settingBtn.setOnClickListener {
//                startActivity(Intent(applicationContext, SettingActivity::class.java))
//            }
        }
    }
    override fun initPref() { Preferences.init(applicationContext) }

    override fun search(str: String) {
        binding.numberInput.setText(str)
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager // 키보드 내리기
        inputMethodManager.hideSoftInputFromWindow(binding.numberInput.windowToken, 0)

        val phoneNum = StringManager.changeToHyphenNumber(str.replace(")", "").replace("-", ""))
        mainPresenter.doSearch(phoneNum)
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