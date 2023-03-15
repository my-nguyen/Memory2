package com.rahulpandey.memory

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.rahulpandey.memory.databinding.ActivityCreateBinding
import java.io.ByteArrayOutputStream

private const val PICK_PHOTO_CODE = 1971
private const val READ_EXTERNAL_PHOTO_CODE = 2015
private const val MIN_GAME_LENGTH = 3
private const val MAX_GAME_LENGTH = 14
private const val READ_PHOTO_PERMISSION = android.Manifest.permission.READ_EXTERNAL_STORAGE
private const val TAG = "CreateActivity-Truong"

class CreateActivity : AppCompatActivity() {
    private lateinit var boardSize: BoardSize
    private lateinit var adapter: ImagesAdapter
    private lateinit var binding: ActivityCreateBinding
    private var requiredImages = -1
    private val imageUris = mutableListOf<Uri>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        boardSize = intent.getSerializableExtra(EXTRA_BOARD_SIZE) as BoardSize
        requiredImages = boardSize.pairs()
        supportActionBar?.title = "Choose pics (0 / $requiredImages)"

        binding.save.setOnClickListener {
            saveDataToFirebase()
        }

        // set game name to a maximum of 14 characters
        binding.gameName.filters = arrayOf(InputFilter.LengthFilter(MAX_GAME_LENGTH))
        binding.gameName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                binding.save.isEnabled = shouldEnableSave()
            }
        })

        adapter = ImagesAdapter(imageUris, boardSize, object : ImagesAdapter.ClickListener {
            override fun onPlaceholderClicked() {
                if (isPermissionGranted(this@CreateActivity, READ_PHOTO_PERMISSION)) {
                    launchImagePicker()
                } else {
                    requestPermission(
                        this@CreateActivity,
                        READ_PHOTO_PERMISSION,
                        READ_EXTERNAL_PHOTO_CODE
                    )
                }
            }
        })
        binding.recycler.adapter = adapter
        binding.recycler.setHasFixedSize(true)
        binding.recycler.layoutManager = GridLayoutManager(this, boardSize.width())
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == READ_EXTERNAL_PHOTO_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchImagePicker()
            } else {
                Toast.makeText(
                    this,
                    "In order to create a custom game, you need to provide access to your photos",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != PICK_PHOTO_CODE || resultCode != Activity.RESULT_OK || data == null) {
            Log.w(
                TAG,
                "Did not get data back from the launched Activity; user likely cancelled  flow"
            )
        } else {
            val selectedUri = data.data
            val clipData = data.clipData
            if (clipData != null) {
                Log.i(TAG, "clipData image count: ${clipData.itemCount} $clipData")
                for (i in 0 until clipData.itemCount) {
                    if (imageUris.size < requiredImages) {
                        imageUris.add(clipData.getItemAt(i).uri)
                    }
                }
            } else if (selectedUri != null) {
                Log.i(TAG, "data: $selectedUri")
                imageUris.add(selectedUri)
            }
            adapter.notifyDataSetChanged()
            supportActionBar?.title = "Choose pics (${imageUris.size} / $requiredImages)"
            binding.save.isEnabled = shouldEnableSave()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun saveDataToFirebase() {
        Log.i(TAG, "saveDataToFirebase")
        for ((i, uri) in imageUris.withIndex()) {
            val byteArray = getImageByteArray(uri)
        }
    }

    private fun shouldEnableSave() =
        imageUris.size == requiredImages && binding.gameName.text.isNotBlank() && binding.gameName.text.length >= MIN_GAME_LENGTH

    private fun getImageByteArray(uri: Uri): ByteArray {
        val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        } else {
            MediaStore.Images.Media.getBitmap(contentResolver, uri)
        }
        Log.i(TAG, "Original width: ${bitmap.width}, height: ${bitmap.height}")
        val scaled = BitmapScaler.scaleToFitWidth(bitmap, 250)
        Log.i(TAG, "Scaled width: ${scaled.width}, height: ${scaled.height}")

        val stream = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, 60, stream)
        return stream.toByteArray()
    }

    private fun launchImagePicker() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        // allow user to select multiple images
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(Intent.createChooser(intent, "Choose pics"), PICK_PHOTO_CODE)
    }
}