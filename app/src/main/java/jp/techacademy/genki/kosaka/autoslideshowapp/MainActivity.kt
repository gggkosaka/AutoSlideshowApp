package jp.techacademy.genki.kosaka.autoslideshowapp

import android.Manifest
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.provider.MediaStore
import android.content.ContentUris
import android.database.Cursor
import android.os.Handler
import kotlinx.android.synthetic.main.activity_main.*

import java.util.* // Timerで使用

class MainActivity : AppCompatActivity() {

    private val PERMISSIONS_REQUEST_CODE = 100

    // タイマー
    private var mTimer: Timer? = null

    private var mHandler = Handler()

    // カーソル
    private var cursor : Cursor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo()
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_CODE)
            }
            // Android 5系以下の場合
        } else {
            getContentsInfo()
        }

        // 進むボタン
        btnNext.setOnClickListener {
            getNextImage()
        }

        // 戻るボタン
        btnPrev.setOnClickListener {
            if(cursor!!.moveToPrevious()){
                Log.d("_dev", "前がある")
                val fieldIndex = cursor!!.getColumnIndex(MediaStore.Images.Media._ID)
                val id = cursor!!.getLong(fieldIndex)
                val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                imageView.setImageURI(imageUri)

            } else {
                cursor!!.moveToLast()
                Log.d("_dev", "前が無い、末尾に戻る")
                val fieldIndex = cursor!!.getColumnIndex(MediaStore.Images.Media._ID)
                val id = cursor!!.getLong(fieldIndex)
                val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                imageView.setImageURI(imageUri)

            }
        }

        // 再生・停止ボタン
        btnRun.setOnClickListener {

            // timer が存在しない場合だけ、タイマーを生成
            if (mTimer == null){
                mTimer = Timer()
                mTimer!!.schedule(object : TimerTask() {
                    override fun run() {
                        mHandler.post {
                            getNextImage()

                        }
                    }
                }, 2000, 2000) // 最初に始動させるまで 2秒、ループの間隔を 2秒 に設定
            }

            // 活性と非活性を交互に
            if(btnNext.isEnabled) {
                // 再生クリック時（＝活性時）はタイトルを「停止」にして非活性に
                btnRun.text = "停止"
                btnNext.isEnabled = false
                btnPrev.isEnabled = false
            } else {
                // 停止クリック時（＝非活性時）はタイトルを「再生」にして活性に
                btnRun.text = "再生"
                btnNext.isEnabled = true
                btnPrev.isEnabled = true

                mTimer!!.cancel()    // タイマーを停止する
                mTimer = null        // mTimer == null で判断するために、 mTimer = null としておく
            }

        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo()
                } else {
                    // 権限が無い場合は非活性にする
                    btnNext.isEnabled = false
                    btnPrev.isEnabled = false
                    btnRun.isEnabled = false

                }
        }
    }

    private fun getContentsInfo() {
        cursor = contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
            null, // 項目(null = 全項目)
            null, // フィルタ条件(null = フィルタなし)
            null, // フィルタ用パラメータ
            null // ソート (null ソートなし)
        )

        if (cursor!!.moveToFirst()) {
            // indexからIDを取得し、そのIDから画像のURIを取得する
            val fieldIndex = cursor!!.getColumnIndex(MediaStore.Images.Media._ID)
            val id = cursor!!.getLong(fieldIndex)
            val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

            Log.d("_dev", "URI : " + imageUri.toString())
            imageView.setImageURI(imageUri)
        } else {
            // 画像が無い場合は非活性にする
            btnNext.isEnabled = false
            btnPrev.isEnabled = false
            btnRun.isEnabled = false

        }

    }

    private fun getNextImage(){
        if(cursor!!.moveToNext()){
            //
            Log.d("_dev", "次がある")
            val fieldIndex = cursor!!.getColumnIndex(MediaStore.Images.Media._ID)
            val id = cursor!!.getLong(fieldIndex)
            val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
            imageView.setImageURI(imageUri)

        } else {
            cursor!!.moveToFirst()
            Log.d("_dev", "次が無い、先頭に戻る")
            val fieldIndex = cursor!!.getColumnIndex(MediaStore.Images.Media._ID)
            val id = cursor!!.getLong(fieldIndex)
            val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
            imageView.setImageURI(imageUri)

        }

    }

    // Activity破棄
    override fun onDestroy() {
        super.onDestroy()

        Log.d("_dev", "onDestroy")

        // カーソルをクローズ
        if(cursor != null){
            cursor!!.close()
        }

        // タイマーをクローズ
        if (mTimer != null){
            mTimer!!.cancel()
            mTimer = null
        }
    }
}
