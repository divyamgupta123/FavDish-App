package com.example.fav_dish.view.activities

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.fav_dish.R
import com.example.fav_dish.application.FavDishApplication
import com.example.fav_dish.databinding.ActivityAddUpdateDishBinding
import com.example.fav_dish.databinding.DialogCustomImageSelectionBinding
import com.example.fav_dish.databinding.DialogCustomListBinding
import com.example.fav_dish.model.entities.FavDish
import com.example.fav_dish.utils.Constants
import com.example.fav_dish.view.adapters.CustomListItemAdapter
import com.example.fav_dish.viewmodel.FavDishViewModel
import com.example.fav_dish.viewmodel.FavDishViewModelFactory
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
import java.io.*
import java.util.*

class AddUpdateDishActivity : AppCompatActivity(), View.OnClickListener {
    private var mImagePath: String = ""
    lateinit var binding: ActivityAddUpdateDishBinding
    lateinit var mCustomListDialog: Dialog

    private var mFavDishDetails: FavDish? = null

    private val mFavDishViewModel: FavDishViewModel by viewModels {
        FavDishViewModelFactory((application as FavDishApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddUpdateDishBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (intent.hasExtra(Constants.EXTRA_DISH_DETAILS)) {
            mFavDishDetails = intent.getParcelableExtra(Constants.EXTRA_DISH_DETAILS)
        }

        setupActionBar()

        mFavDishDetails?.let {
            if (it.id != 0) {
                mImagePath = it.image
                Glide.with(this)
                    .load(mImagePath)
                    .centerCrop()
                    .into(binding.ivDishImage)

                binding.etTitle.setText(it.title)
                binding.etType.setText(it.type)
                binding.etCategory.setText(it.category)
                binding.etIngredients.setText(it.ingredients)
                binding.etCookingTime.setText(it.cookingTime)
                binding.etDirectionToCook.setText(it.directionToCook)

                binding.btnAddDish.text = resources.getString(R.string.lbl_update_dish)
            }
        }

        binding.ivAddDishImage.setOnClickListener(this)

        binding.etType.setOnClickListener(this)
        binding.etCategory.setOnClickListener(this)
        binding.etCookingTime.setOnClickListener(this)

        binding.btnAddDish.setOnClickListener(this)
    }

    private fun setupActionBar() {
        setSupportActionBar(binding.toolbarAddDishActivity)

        if (mFavDishDetails != null && mFavDishDetails!!.id != 0) {
            supportActionBar?.let {
                it.title = resources.getString(R.string.title_edit_dish)
            }
        } else {
            supportActionBar?.let {
                it.title = resources.getString(R.string.title_add_dish)
            }
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbarAddDishActivity.setNavigationOnClickListener() {
            onBackPressed()
        }
    }

    override fun onClick(v: View?) {
        if (v != null) {
            when (v.id) {
                R.id.iv_add_dish_image -> {
                    customImageSelectionDialog()
                    return
                }
                R.id.et_type -> {
                    customItemListDialog(
                        resources.getString(R.string.title_select_dish_type),
                        Constants.dishTypes(),
                        Constants.DISH_TYPE
                    )
                    return
                }
                R.id.et_category -> {
                    customItemListDialog(
                        resources.getString(R.string.title_select_dish_category),
                        Constants.dishCategories(),
                        Constants.DISH_CATEGORY
                    )
                    return
                }
                R.id.et_cooking_time -> {
                    customItemListDialog(
                        resources.getString(R.string.title_select_dish_cooking_time),
                        Constants.dishCookingTime(),
                        Constants.DISH_COOKING_TIME
                    )
                    return
                }
                R.id.btn_add_dish -> {
                    val title = binding.etTitle.text.toString().trim { it <= ' ' }
                    val type = binding.etType.text.toString().trim { it <= ' ' }
                    val category = binding.etCategory.text.toString().trim { it <= ' ' }
                    val ingredients = binding.etIngredients.text.toString().trim { it <= ' ' }
                    val cookingTimeInMinutes =
                        binding.etCookingTime.text.toString().trim { it <= ' ' }
                    val cookingDirection =
                        binding.etDirectionToCook.text.toString().trim { it <= ' ' }

                    when {

                        TextUtils.isEmpty(mImagePath) -> {
                            Toast.makeText(
                                this@AddUpdateDishActivity,
                                resources.getString(R.string.err_msg_select_dish_image),
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        TextUtils.isEmpty(title) -> {
                            Toast.makeText(
                                this@AddUpdateDishActivity,
                                resources.getString(R.string.err_msg_enter_dish_title),
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        TextUtils.isEmpty(type) -> {
                            Toast.makeText(
                                this@AddUpdateDishActivity,
                                resources.getString(R.string.err_msg_select_dish_type),
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        TextUtils.isEmpty(category) -> {
                            Toast.makeText(
                                this@AddUpdateDishActivity,
                                resources.getString(R.string.err_msg_select_dish_category),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        TextUtils.isEmpty(ingredients) -> {
                            Toast.makeText(
                                this@AddUpdateDishActivity,
                                resources.getString(R.string.err_msg_enter_dish_ingredients),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        TextUtils.isEmpty(cookingTimeInMinutes) -> {
                            Toast.makeText(
                                this@AddUpdateDishActivity,
                                resources.getString(R.string.err_msg_select_dish_cooking_time),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        TextUtils.isEmpty(cookingDirection) -> {
                            Toast.makeText(
                                this@AddUpdateDishActivity,
                                resources.getString(R.string.err_msg_enter_dish_cooking_instructions),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        else -> {
                            var dishID = 0
                            var imageSource = Constants.DISH_IMAGE_SOURCE_LOCAL
                            var favoriteDish = false

                            mFavDishDetails?.let {
                                if (it.id != 0) {
                                    dishID = it.id
                                    imageSource = it.imageSource
                                    favoriteDish = it.favoriteDish
                                }
                            }

                            val favDishDetails: FavDish = FavDish(
                                mImagePath,
                                imageSource,
                                title,
                                type,
                                category,
                                ingredients,
                                cookingTimeInMinutes,
                                cookingDirection,
                                favoriteDish,
                                dishID
                            )
                            if (dishID == 0) {
                                mFavDishViewModel.insert(favDishDetails)

                                Toast.makeText(
                                    this@AddUpdateDishActivity,
                                    "You successfully added your favorite dish details.",
                                    Toast.LENGTH_SHORT
                                ).show()

                                // You even print the log if Toast is not displayed on emulator
                                Log.e("Insertion", "Success")
                            } else {
                                mFavDishViewModel.update(favDishDetails)

                                Toast.makeText(
                                    this@AddUpdateDishActivity,
                                    "You successfully updated your favorite dish details.",
                                    Toast.LENGTH_SHORT
                                ).show()

                                // You even print the log if Toast is not displayed on emulator
                                Log.e("Updating", "Success")
                            }
                            finish()
                        }
                    }
                }
            }
        }
    }

    private fun customImageSelectionDialog() {
        val dialog = Dialog(this)
        val binding: DialogCustomImageSelectionBinding =
            DialogCustomImageSelectionBinding.inflate(layoutInflater)
        dialog.setContentView(binding.root)

        binding.tvCamera.setOnClickListener {

            Dexter.withContext(this).withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            ).withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    report?.let {
                        if (report.areAllPermissionsGranted()) {
                            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                            startActivityForResult(intent, CAMERA)
                        }
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    tokens: PermissionToken?
                ) {
                    showRationalDialogForPermissions()
                }
            }
            ).onSameThread().check()

            dialog.dismiss()
        }
        binding.tvGallery.setOnClickListener {
            Dexter.withContext(this).withPermission(
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
                .withListener(object : PermissionListener {

                    override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                        val galleryIntent =
                            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        startActivityForResult(galleryIntent, GALLERY)
                    }

                    override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                        Toast.makeText(
                            this@AddUpdateDishActivity,
                            "You denied gallery permissions",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        p0: PermissionRequest?,
                        p1: PermissionToken?
                    ) {
                        showRationalDialogForPermissions()
                    }

                }).onSameThread().check()
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == CAMERA) {
                data?.extras?.let {
                    val thumbnail: Bitmap = data.extras!!.get("data") as Bitmap
                    Glide.with(this)
                        .load(thumbnail)
                        .centerCrop()
                        .into(binding.ivDishImage)

                    mImagePath = saveImageToInternalStorage(thumbnail)

                    Log.i("Image Path", mImagePath)
                    binding.ivAddDishImage.setImageDrawable(
                        ContextCompat.getDrawable(
                            this,
                            R.drawable.ic_vector_edit
                        )
                    )
                }
            }
            if (requestCode == GALLERY) {
                data?.let {
                    val selectedPhotoUri = data.data

                    Glide.with(this)
                        .load(selectedPhotoUri)
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .listener(object : RequestListener<Drawable> {
                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any?,
                                target: Target<Drawable>?,
                                isFirstResource: Boolean
                            ): Boolean {
                                Log.e("TAG", "Error Loading Image")
                                return false
                            }

                            override fun onResourceReady(
                                resource: Drawable?,
                                model: Any?,
                                target: Target<Drawable>?,
                                dataSource: DataSource?,
                                isFirstResource: Boolean
                            ): Boolean {
                                resource?.let {
                                    val bitmap: Bitmap = resource.toBitmap()
                                    mImagePath = saveImageToInternalStorage(bitmap)
                                    Log.i("Image Path", mImagePath)
                                }
                                return false
                            }

                        })
                        .into(binding.ivDishImage)

                    binding.ivAddDishImage.setImageDrawable(
                        ContextCompat.getDrawable(
                            this,
                            R.drawable.ic_vector_edit
                        )
                    )
                }
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            Log.e("cancelled", "User Cancelled")
        }
    }

    private fun showRationalDialogForPermissions() {
        AlertDialog.Builder(this)
            .setMessage("It looks like you have turned off permissions required for this feature. It can be enabled under Application settings.")
            .setPositiveButton("Go to settings ") { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap): String {
        val wrapper = ContextWrapper(applicationContext)

        var file = wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)
        file = File(file, "${UUID.randomUUID()}.jpg")
        try {
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return file.absolutePath
    }

    private fun customItemListDialog(title: String, itemList: List<String>, selection: String) {
        mCustomListDialog = Dialog(this@AddUpdateDishActivity)
        val binding: DialogCustomListBinding = DialogCustomListBinding.inflate(layoutInflater)
        mCustomListDialog.setContentView(binding.root)
        binding.tvTitle.text = title

        binding.rvList.layoutManager = LinearLayoutManager(this@AddUpdateDishActivity)

        val adapter = CustomListItemAdapter(this@AddUpdateDishActivity, null, itemList, selection)
        binding.rvList.adapter = adapter
        mCustomListDialog.show()
    }

    fun selectedListItem(item: String, selection: String) {
        when (selection) {
            Constants.DISH_TYPE -> {
                mCustomListDialog.dismiss()
                Log.e("Dialog Dismiss", "Dialog Dismiss")
                binding.etType.setText(item)
                return
            }
            Constants.DISH_CATEGORY -> {
                mCustomListDialog.dismiss()
                binding.etCategory.setText(item)
                return
            }
            Constants.DISH_COOKING_TIME -> {
                mCustomListDialog.dismiss()
                binding.etCookingTime.setText(item)
                return
            }
        }
    }

    companion object {
        private const val CAMERA = 1
        private const val GALLERY = 2
        private const val IMAGE_DIRECTORY = "FavDishImages"

    }
}