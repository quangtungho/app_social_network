package vn.techres.line.fragment.qr

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.socket.client.Socket
import net.glxn.qrgen.android.QRCode
import vn.techres.line.R
import vn.techres.line.activity.BillActivity
import vn.techres.line.activity.MainActivity
import vn.techres.line.activity.TechResApplicationNodeJs
import vn.techres.line.base.BaseBindingFragment
import vn.techres.line.data.model.RealtimeBillOrder
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.FragmentCodeBarBinding
import vn.techres.line.helper.CurrentUser
import vn.techres.line.helper.WriteLog
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.helper.utils.AlolineColorUtil

@SuppressLint("UseRequireInsteadOfGet")
class CodeBarFragment : BaseBindingFragment<FragmentCodeBarBinding>(FragmentCodeBarBinding::inflate) {
    private val mainActivity: MainActivity?
        get() = activity as MainActivity?
    
    private var user = User()
    private var mSocket: Socket? = null
    private var keyRealtime: String = ""
    private var techResApplicationNodeJs = TechResApplicationNodeJs()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        user = CurrentUser.getCurrentUser(requireActivity())
        mSocket = techResApplicationNodeJs.getSocketInstance(requireActivity())
        mSocket?.connect()
        keyRealtime = String.format(
            "%s/%s/%s",
            "customers",
            user.id.toString(),
            "orders"
        )
        setSocketEmit()
    }

    override fun onResume() {
        super.onResume()
        mainActivity?.setOnBackClick(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        mainActivity?.removeOnBackClick(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.window?.statusBarColor = AlolineColorUtil(view.context).convertColor(R.color.white)
        setBarcode()

        mSocket?.emit("join_room", keyRealtime)
        binding.imgBack.setOnClickListener {
            mainActivity?.supportFragmentManager?.popBackStack()
        }
    }
    
    private fun unRegisterSocket() {
        mSocket?.emit("leave_room", keyRealtime)
        WriteLog.d("leave_room", "leave_room")
        mSocket?.disconnect()
    }

    private fun setSocketEmit() {
        mSocket?.on(keyRealtime) { args ->
            mainActivity?.runOnUiThread {
                WriteLog.d("key_realtime", args[0].toString())
                val obj = Gson().fromJson<RealtimeBillOrder>(args[0].toString(), object :
                    TypeToken<RealtimeBillOrder>() {}.type)
                unRegisterSocket()
                val intent = Intent(mainActivity, BillActivity::class.java)
                intent.putExtra(TechresEnum.KEY_ID_ORDER_CUSTOMER.toString(), obj.id ?: 0)
                startActivity(intent)
                mainActivity!!.overridePendingTransition(
                    R.anim.translate_from_right,
                    R.anim.translate_to_right
                )
            }
        }
    }

    private fun setBarcode() {
        if (user.address.isEmpty()) {
            try {
                val bitmap =
                    QRCode.from(
                        String.format("%s:%s:%s:%s", user.id, user.phone, user.name, " ")
                    ).withSize(200, 200)
                        .withCharset("UTF-8").bitmap()
                binding.imgBarcode.setImageBitmap(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            try {
                val bitmap =
                    QRCode.from(
                        String.format(
                            "%s:%s:%s:%s",
                            user.id,
                            user.phone,
                            user.name,
                            user.address
                        )
                    ).withSize(200, 200).withCharset("UTF-8").bitmap()
                binding.imgBarcode.setImageBitmap(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onBackPress() : Boolean{
        unRegisterSocket()
        return true

    }
}
