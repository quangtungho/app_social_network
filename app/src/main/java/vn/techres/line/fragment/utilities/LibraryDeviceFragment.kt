package vn.techres.line.fragment.utilities

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Environment
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.R
import vn.techres.line.activity.MainActivity
import vn.techres.line.adapter.librarydevice.LibraryDeviceAdapter
import vn.techres.line.base.BaseBindingFragment
import vn.techres.line.data.model.librarydevice.FileDevice
import vn.techres.line.databinding.FragmentLibraryDeviceBinding
import vn.techres.line.helper.utils.AlolineColorUtil
import vn.techres.line.interfaces.librarydevice.LibraryDeviceHandler
import java.io.File
import kotlin.math.ceil

@SuppressLint("UseRequireInsteadOfGet")
class LibraryDeviceFragment : BaseBindingFragment<FragmentLibraryDeviceBinding>(
    FragmentLibraryDeviceBinding::inflate), LibraryDeviceHandler {
    private val mainActivity: MainActivity?
        get() = activity as MainActivity?
    private var fileList = ArrayList<File>()
    private var fileLimitList = ArrayList<FileDevice>()
    private var adapter : LibraryDeviceAdapter? = null
    private var limit = 20
    private var page = 1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.window?.statusBarColor = AlolineColorUtil(view.context).convertColor(R.color.white)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.rcLibraryDevice.layoutManager = GridLayoutManager(mainActivity!!.baseContext, 3)
        adapter = LibraryDeviceAdapter(mainActivity!!)
        adapter!!.setLibraryDeviceHandler(this)
        binding.rcLibraryDevice.adapter = adapter
        val acceptedExtensions = listOf("jpg", "mp4", "png", "jpeg")
        val rootDir = Environment.getExternalStorageDirectory().absolutePath
        val androidDir = File("$rootDir/Android")
        val dataDir = File("$rootDir/data")
        fileList = File(rootDir).walk()
            // befor entering this dir check if
            .onEnter{ !it.isHidden // it is not hidden
                    && it != androidDir // it is not Android directory
                    && it != dataDir // it is not data directory
                    && !File(it, ".nomedia").exists() //there is no .nomedia file inside
            }.filter { acceptedExtensions.indexOf(it.extension) > -1}// it is of accepted type
            .toList().reversed() as ArrayList<File>
        setData()
    }

    @SuppressLint("NewApi")
    private fun setData(){
        fileLimitList.add(0, FileDevice())
        fileLimitList.addAll(fileLimitList.size, getLimitFile(limit, page, fileList))
        adapter!!.setDataSource(fileLimitList)
        binding.rcLibraryDevice.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            var y = 0
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                y = dy
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(1) && RecyclerView.SCROLL_STATE_IDLE == newState) {
                    if (y <= 0) {
                        if (page < ceil((fileList.size / limit).toDouble())) {
                            val positionStart = fileLimitList.size
                            page++
                            fileLimitList.addAll(
                                fileLimitList.size, getLimitFile(
                                    limit,
                                    page,
                                    fileList
                                )
                            )
                            val positionEnd = fileLimitList.size
                            adapter!!.notifyItemMoved(positionStart, positionEnd)
                        }
                    } else {
                        y = 0
                    }
                }
            }
        })
        //data = mainActivity!!.contentResolver.query(queryUri, null, null, null)
    }
    private fun getLimitFile(limit: Int, page: Int, arrayList: ArrayList<File>) : ArrayList<FileDevice>{
        val position = (page - 1)*limit
        val fileList = ArrayList<FileDevice>()
        for (i in position..arrayList.size){
            if(i <= (position + limit - 1)){
                val file = arrayList[position]
                val fileDevice = FileDevice()
                fileDevice.file_name = file.name
                fileDevice.path = file.path
                if(file.name.contains("mp4")){
                    fileDevice.type = "video"
                }else{
                    fileDevice.type = "image"
                }
                fileList.add(fileDevice)
            }else{
                break
            }
        }
        return fileList
    }
    override fun onChooseFile(file: FileDevice) {

    }

    override fun onChooseCamera() {

    }

    override fun onBackPress() : Boolean {
        return true
    }
}