package vn.techres.line.helper

/* import com.google.firebase.iid.FirebaseInstanceIdService */
import android.content.Context
import java.io.*

class InternalStorage() {

    //    private var entries: List<Entry> = arrayListOf()
     private val entries: MutableMap<String, String> = mutableMapOf()
     init {
         /*
         *  every time init is called increment instance count
         *  just in case somehow we break singleton rule, this will be
         *  called more than once and myInstancesCount > 1 == true
         */
         ++myInstancesCount
     }
     companion object {
         //Debuggable field to check instance count
         var myInstancesCount = 0;
         private val mInstance: InternalStorage = InternalStorage()

         @Synchronized
         fun getInstance(): InternalStorage {
             return mInstance
         }
     }

    @Throws(IOException::class)
    fun writeObject(context: Context, key: String?, `object`: Any?) {
        when {
            key!!.isNotEmpty() -> entries[key] = `object`.toString()
            else -> throw IllegalArgumentException("Key cannot be empty")
        }
        val fos: FileOutputStream = context.openFileOutput(key, Context.MODE_PRIVATE)
        val oos = ObjectOutputStream(fos)
        oos.writeObject(`object`)
        oos.close()
        fos.close()
    }

    @Throws(IOException::class, ClassNotFoundException::class)
    fun readObject(context: Context, key: String?): Any? {
        return try{
            val fis: FileInputStream = context.openFileInput(key)
            val ois = ObjectInputStream(fis)
            ois.readObject()
        }catch (e: Exception){
//            Toast.makeText(context,"Bạn chưa lưu",Toast.LENGTH_LONG).show()
        }


    }




    @Throws(IOException::class, ClassNotFoundException::class)
    fun clearCache(key: String?) {
        val file = File(key!!)
        file.delete()
        file.deleteOnExit()
    }
//     fun save(context: Context,save: String) {
//         val text: String = save //luu vao storage
//         var fos: FileOutputStream? = null
//         try {
//             fos = context.openFileOutput(FILE_NAME, FirebaseInstanceIdService.MODE_PRIVATE)
//             fos!!.write(text.toByteArray())
//
//         } catch (e: FileNotFoundException) {
//             e.printStackTrace()
//         } catch (e: IOException) {
//             e.printStackTrace()
//         } finally {
//             if (fos != null) {
//                 try {
//                     fos.close()
//                 } catch (e: IOException) {
//                     e.printStackTrace()
//                 }
//             }
//         }
//     }

//     fun load(context: Context,v: View?) {
//         var fis: FileInputStream? = null
//         try {
//             fis = context.openFileInput(FILE_NAME)
//             val isr = InputStreamReader(fis!!)
//             val br = BufferedReader(isr)
//             val sb = StringBuilder()
//             var text: String? = null
//             while (br.readLine().also({ text = it }) != null) {
//                 sb.append(text).append("\n")
//             }
////            mEditText.setText(sb.toString()) // lay storage ra
//             Log.d(TAG, "Token_load: ${sb}")
//         } catch (e: FileNotFoundException) {
//             e.printStackTrace()
//         } catch (e: IOException) {
//             e.printStackTrace()
//         } finally {
//             if (fis != null) {
//                 try {
//                     fis.close()
//                 } catch (e: IOException) {
//                     e.printStackTrace()
//                 }
//             }
//         }
//
//
//     }


}