package com.example.fav_dish.view.fragments

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.*
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.palette.graphics.Palette
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.fav_dish.R
import com.example.fav_dish.application.FavDishApplication
import com.example.fav_dish.databinding.FragmentDishDetailsBinding
import com.example.fav_dish.model.entities.FavDish
import com.example.fav_dish.utils.Constants
import com.example.fav_dish.viewmodel.FavDishViewModel
import com.example.fav_dish.viewmodel.FavDishViewModelFactory
import java.io.IOException
import java.util.*

class DishDetailsFragment : Fragment() {


    private var _binding: FragmentDishDetailsBinding? = null

    private val mFavDishViewModel: FavDishViewModel by viewModels {
        FavDishViewModelFactory(((requireActivity().application) as FavDishApplication).repository)
    }

    private var mFavDishDetails: FavDish? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_share, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.action_share_dish -> {
                val type = "text/plain"
                val subject = "Checkout this dish recipe"
                var extraText = ""
                val shareWith = "Share with"

                mFavDishDetails?.let {
                    var image = ""
                    if (it.imageSource == Constants.DISH_IMAGE_SOURCE_ONLINE) {
                        image = it.image
                    }
                    var cookingIntruction = ""
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        cookingIntruction = Html.fromHtml(
                            it.directionToCook,
                            Html.FROM_HTML_MODE_COMPACT
                        ).toString()
                    } else {
                        @Suppress("DEPRECATION")
                        cookingIntruction = Html.fromHtml(it.directionToCook).toString()
                    }
                    extraText =
                        "$image \n" +
                                "\n Title:  ${it.title} \n\n Type: ${it.type} \n\n Category: ${it.category}" +
                                "\n\n Ingredients: \n ${it.ingredients} \n\n Instructions To Cook: \n $it.cookingInstructions" +
                                "\n\n Time required to cook the dish approx ${it.cookingTime} minutes."
                }
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = type
                intent.putExtra(Intent.EXTRA_SUBJECT, subject)
                intent.putExtra(Intent.EXTRA_TEXT, extraText)
                startActivity(Intent.createChooser(intent, shareWith))
                return true
            }
        }
        return super.onOptionsItemSelected(item)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentDishDetailsBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args: DishDetailsFragmentArgs by navArgs()

        mFavDishDetails = args.dishDetails

        args.let {
            try {
                Glide.with(requireActivity())
                    .load(it.dishDetails.image)
                    .centerCrop()
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            resource.let {
                                Palette.from(resource!!.toBitmap()).generate { palette ->
                                    val intColor = palette?.vibrantSwatch?.rgb ?: 0
                                    binding.rlDishDetailMain.setBackgroundColor(intColor)
                                }

                            }
                            return false
                        }
                    })
                    .into(binding.ivDishImage)
            } catch (e: IOException) {
                e.printStackTrace()
            }

            binding.tvTitle.text = it.dishDetails.title
            binding.tvType.text = it.dishDetails.type.capitalize(Locale.ROOT)
            binding.tvCategory.text = it.dishDetails.category
            binding.tvIngredients.text = it.dishDetails.ingredients
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                binding.tvCookingDirection.text = Html.fromHtml(
                    it.dishDetails.directionToCook,
                    Html.FROM_HTML_MODE_COMPACT
                )
            } else {
                @Suppress("DEPRECATION")
                binding.tvCookingDirection.text = Html.fromHtml(it.dishDetails.directionToCook)
            }


            binding.tvCookingTime.text =
                resources.getString(R.string.lbl_estimate_cooking_time, it.dishDetails.cookingTime)

            if (args.dishDetails.favoriteDish) {
                binding.ivFavoriteDish.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireActivity(),
                        R.drawable.ic_favorite_selected
                    )
                )
            } else {
                binding.ivFavoriteDish.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireActivity(),
                        R.drawable.ic_favorite_unselected
                    )
                )
            }

            binding.ivFavoriteDish.setOnClickListener {
                args.dishDetails.favoriteDish = !args.dishDetails.favoriteDish
                mFavDishViewModel.update(args.dishDetails)

                if (args.dishDetails.favoriteDish) {
                    binding.ivFavoriteDish.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireActivity(),
                            R.drawable.ic_favorite_selected
                        )
                    )
                    Toast.makeText(requireActivity(), "Dish added to favorite", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    binding.ivFavoriteDish.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireActivity(),
                            R.drawable.ic_favorite_unselected
                        )
                    )
                    Toast.makeText(
                        requireActivity(),
                        "Dish removed from favorite",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}