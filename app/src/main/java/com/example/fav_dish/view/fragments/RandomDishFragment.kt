package com.example.fav_dish.view.fragments

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.fav_dish.R
import com.example.fav_dish.application.FavDishApplication
import com.example.fav_dish.databinding.FragmentRandomDishBinding
import com.example.fav_dish.model.entities.FavDish
import com.example.fav_dish.model.entities.RandomDish
import com.example.fav_dish.utils.Constants
import com.example.fav_dish.viewmodel.FavDishViewModel
import com.example.fav_dish.viewmodel.FavDishViewModelFactory
import com.example.fav_dish.viewmodel.RandomDishViewModel

class RandomDishFragment : Fragment() {

    private var _binding: FragmentRandomDishBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var mRandomDishViewModel: RandomDishViewModel

    private var mProgressDialog: Dialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentRandomDishBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mRandomDishViewModel = ViewModelProvider(this).get(RandomDishViewModel::class.java)
        mRandomDishViewModel.getRandomRecipeFromApi()
        randomDishViewModelObserver()

        binding.srlRandomDish.setOnRefreshListener {
            mRandomDishViewModel.getRandomRecipeFromApi()
        }
    }

    private fun randomDishViewModelObserver() {
        mRandomDishViewModel.randomDishResponse.observe(viewLifecycleOwner, { randomDishResponse ->
            randomDishResponse?.let {
                if (binding.srlRandomDish.isRefreshing) {
                    binding.srlRandomDish.isRefreshing = false
                }
                setRandomDishResponseInUI(randomDishResponse.recipes[0])
            }
        })
        mRandomDishViewModel.randomDishLoadingError.observe(viewLifecycleOwner, { dataError ->
            dataError?.let {
                Log.e("Random Dish API Error", "$dataError")
            }
        })

        mRandomDishViewModel.loadRandomDish.observe(viewLifecycleOwner, { loadRandomDish ->
            loadRandomDish?.let {
                Log.e("Random Dish Loading", "$loadRandomDish")

                if (loadRandomDish && !binding.srlRandomDish.isRefreshing) {
                    showCustomProgressDialog()
                } else {
                    hideProgressDialog()
                }

            }
        })
    }

    private fun setRandomDishResponseInUI(recipe: RandomDish.Recipe) {
        Glide.with(this)
            .load(recipe.image)
            .centerCrop()
            .into(binding.ivDishImage)

        binding.tvTitle.text = recipe.title

        var dishType = "other"
        if (recipe.dishTypes.isNotEmpty()) {
            dishType = recipe.dishTypes[0]
        }
        binding.tvType.text = dishType

        binding.tvCategory.text = "Other"

        var ingredients = ""
        for (value in recipe.extendedIngredients) {

            ingredients = if (ingredients.isEmpty()) {
                value.original
            } else {
                ingredients + ", \n" + value.original
            }
        }

        binding.tvIngredients.text = ingredients

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            binding.tvCookingDirection.text = Html.fromHtml(
                recipe.instructions,
                Html.FROM_HTML_MODE_COMPACT
            )
        } else {
            @Suppress("DEPRECATION")
            binding.tvCookingDirection.text = Html.fromHtml(recipe.instructions)
        }

        binding.tvCookingTime.text =
            resources.getString(
                R.string.lbl_estimate_cooking_time,
                recipe.readyInMinutes.toString()
            )

        binding.ivFavoriteDish.setImageDrawable(
            ContextCompat.getDrawable(
                requireActivity(),
                R.drawable.ic_favorite_unselected
            )
        )

        var addedToFavorite = false

        binding.ivFavoriteDish.setOnClickListener {
            if (addedToFavorite) {
                Toast.makeText(
                    requireActivity(),
                    resources.getString(R.string.msg_already_added_to_favorites),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val randomDishDetails = FavDish(
                    recipe.image,
                    Constants.DISH_IMAGE_SOURCE_ONLINE,
                    recipe.title,
                    dishType,
                    "Other",
                    ingredients,
                    recipe.readyInMinutes.toString(),
                    recipe.instructions,
                    true
                )
                val mFavDishViewModel: FavDishViewModel by viewModels {
                    FavDishViewModelFactory((requireActivity().application as FavDishApplication).repository)
                }
                mFavDishViewModel.insert(randomDishDetails)

                addedToFavorite = true

                binding.ivFavoriteDish.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireActivity(),
                        R.drawable.ic_favorite_selected
                    )
                )
                Toast.makeText(
                    requireActivity(),
                    resources.getString(R.string.msg_added_to_favorites),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun showCustomProgressDialog() {
        mProgressDialog = Dialog(requireActivity())
        mProgressDialog?.let {
            it.setContentView(R.layout.dialog_custom_progress)
            it.show()
        }
    }

    private fun hideProgressDialog() {
        mProgressDialog?.let {
            it.dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}