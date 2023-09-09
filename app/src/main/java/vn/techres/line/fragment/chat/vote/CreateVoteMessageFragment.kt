package vn.techres.line.fragment.chat.vote

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.socket.client.Socket
import org.json.JSONException
import org.json.JSONObject
import vn.techres.line.R
import vn.techres.line.activity.TechResApplication
import vn.techres.line.adapter.chat.vote.CreateVoteMessageAdapter
import vn.techres.line.base.BaseBindingChatFragment
import vn.techres.line.data.model.chat.Group
import vn.techres.line.data.model.chat.MessagesByGroup
import vn.techres.line.data.model.chat.OptionVote
import vn.techres.line.data.model.chat.Vote
import vn.techres.line.data.model.chat.request.PinnedRequest
import vn.techres.line.data.model.chat.request.group.CreateVoteRequest
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.FragmentCreateVoteMessageBinding
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.helper.CurrentUser
import vn.techres.line.helper.Utils.getRandomString
import vn.techres.line.helper.WriteLog
import vn.techres.line.helper.techresenum.TechResEnumChat
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.interfaces.chat.CreateVoteMessageHandler
import java.util.*

class CreateVoteMessageFragment :
    BaseBindingChatFragment<FragmentCreateVoteMessageBinding>(FragmentCreateVoteMessageBinding::inflate, true),
    CreateVoteMessageHandler {

    private var deadline = ""
    private var isCheckPinned = false
    private var isCheckTime = 0
    private var group = Group()
    private var listOptionVote = ArrayList<OptionVote>()

    //socket
    private val application = TechResApplication()
    private var configNodeJs = ConfigNodeJs()
    private var user = User()
    private var mSocket: Socket? = null

    private lateinit var adapter: CreateVoteMessageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mSocket = application.getSocketInstance(requireActivity().baseContext)
        mSocket?.connect()
        configNodeJs = CurrentConfigNodeJs.getConfigNodeJs(requireActivity().baseContext)
        user = CurrentUser.getCurrentUser(requireActivity().baseContext)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = CreateVoteMessageAdapter(requireActivity().baseContext)
        adapter.setCreateVoteMessageHandler(this)
        binding.rcPlan.layoutManager =
            LinearLayoutManager(requireActivity().baseContext, RecyclerView.VERTICAL, false)
        binding.rcPlan.itemAnimator?.changeDuration = 0
        (Objects.requireNonNull(binding.rcPlan.itemAnimator) as SimpleItemAnimator).supportsChangeAnimations =
            false
        binding.rcPlan.adapter = adapter
        setData()
        setListener()
    }

    private fun setData() {
        arguments?.let {
            group = Gson().fromJson(
                it.getString(
                    TechresEnum.GROUP_CHAT.toString()
                ), object :
                    TypeToken<Group>() {}.type
            )
        }

        for (i in 0..1) {
            val optionVote = OptionVote()
            optionVote.content = ""
            optionVote.id = i + 1
            listOptionVote.add(optionVote)
        }
        adapter.setDataSource(listOptionVote)
        /**
         * set radio button
         * */
        isCheckTime = R.id.rbUnlimitedTime
        deadline = requireActivity().resources?.getString(R.string.unlimited_time) ?: ""
        binding.rbChoosePinnedVote.isChecked = isCheckPinned
    }

    private fun setListener() {
        binding.tvCreateVote.setOnClickListener {
            val vote = Vote()
            vote.title = (binding.edtMakeQuestion.text ?: "").toString()
            vote.list_option = adapter.getDataSource()
            vote.multi_chose = if (binding.swChoosePlans.isChecked) {
                1
            } else {
                0
            }
            vote.allow_add_option = if (binding.swAddPlan.isChecked) {
                1
            } else {
                0
            }
            vote.deadline = if (deadline == (requireActivity().resources?.getString(R.string.unlimited_time) ?: "")) {
                    TechResEnumChat.UNLIMITED_TIME.toString()
                } else {
                    ""
                }

            createVote(vote)
        }

        binding.rbChoosePinnedVote.setOnClickListener {
            binding.rbChoosePinnedVote.isChecked = !isCheckPinned
        }

        binding.tvSetting.setOnClickListener {
            binding.groupSetting.visibility = View.VISIBLE
            binding.tvSetting.visibility = View.GONE
        }

        binding.tvAddPlan.setOnClickListener {
            if (listOptionVote.size > 20){
                val optionVote = OptionVote()
                optionVote.content = ""
                optionVote.id = listOptionVote.size
                listOptionVote.add(optionVote)
                adapter.notifyItemChanged(listOptionVote.size)
            }else{
                Toast.makeText(requireActivity().baseContext, requireActivity().resources?.getString(R.string.add_limited_plan) ?: "", Toast.LENGTH_LONG).show()
            }
        }

        binding.lnTimeVote.setOnClickListener {
            bottomSheetDialog()
        }
        binding.imgBack.setOnClickListener {
            activityChat?.supportFragmentManager?.popBackStack()
        }
    }

    private fun bottomSheetDialog() {
        val bottomSheetDialog = BottomSheetDialog(requireActivity(), R.style.SheetDialog)
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_set_up_time_vote)
        bottomSheetDialog.setCancelable(true)
        bottomSheetDialog.findViewById<RadioButton>(R.id.rbChooseTheEndTime)
        bottomSheetDialog.findViewById<RadioButton>(R.id.rbUnlimitedTime)
        val radioGroup = bottomSheetDialog.findViewById<RadioGroup>(R.id.radioGroup)
        val btnDone = bottomSheetDialog.findViewById<Button>(R.id.btnDone)
        val imgClose = bottomSheetDialog.findViewById<ImageButton>(R.id.imgClose)
        radioGroup?.check(isCheckTime)

        radioGroup?.setOnCheckedChangeListener { _, checkedId ->
            isCheckTime = checkedId
            when (checkedId) {
                R.id.rbChooseTheEndTime -> {
                    bottomSheetDatePicker()
                }
                R.id.rbUnlimitedTime -> {
                    deadline = requireActivity().resources?.getString(R.string.unlimited_time) ?: ""

                }
            }
        }
        btnDone?.setOnClickListener {
            binding.tvStatusTimeVote.text = deadline
            bottomSheetDialog.dismiss()
        }
        imgClose?.setOnClickListener {
            bottomSheetDialog.dismiss()
        }
        bottomSheetDialog.show()
    }

    private fun bottomSheetDatePicker() {
        val bottomSheetDialog = BottomSheetDialog(requireActivity(), R.style.SheetDialog)
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_date_picker)
        bottomSheetDialog.setCancelable(false)
        bottomSheetDialog.findViewById<ImageView>(R.id.imgClose)
        bottomSheetDialog.findViewById<Button>(R.id.btnConfirm)
        bottomSheetDialog.findViewById<com.ycuwq.datepicker.date.DatePicker>(R.id.datePicker)
        bottomSheetDialog.show()
    }

    private fun pinned(messagesByGroup: MessagesByGroup) {
        val pinnedRequest = PinnedRequest()
        pinnedRequest.random_key = messagesByGroup.random_key
        pinnedRequest.member_id = user.id
        pinnedRequest.group_id = group._id
        pinnedRequest.message_type = TechResEnumChat.TYPE_PINNED.toString()
        pinnedRequest.key_message_error = getRandomString(10)
        try {
            val jsonObject = JSONObject(Gson().toJson(pinnedRequest))
            mSocket?.emit(TechResEnumChat.PINNED_MESSAGE_ALO_LINE.toString(), jsonObject)
            WriteLog.d("PINNED_MESSAGE_ALO_LINE", jsonObject.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun createVote(vote: Vote) {

        val createVoteRequest = CreateVoteRequest()
        createVoteRequest.member_id = user.id
        createVoteRequest.group_id = group._id
        createVoteRequest.message_type = TechResEnumChat.TYPE_VOTE.toString()
        createVoteRequest.key_message_error = getRandomString(10)
        val optionVote = OptionVote()
        optionVote.content = ""
        optionVote.id = -1
        vote.list_option.add(optionVote)
        createVoteRequest.message_vote = vote
        try {
            val jsonObject = JSONObject(Gson().toJson(createVoteRequest))
            mSocket?.emit(TechResEnumChat.CREATE_VOTE_ALO_LINE.toString(), jsonObject)
            WriteLog.d("CREATE_VOTE_ALO_LINE", jsonObject.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    override fun onRemove(position : Int) {
        listOptionVote.removeAt(position)
        adapter.notifyDataSetChanged()
    }

    override fun onBackPress() : Boolean{
        return true
    }
}