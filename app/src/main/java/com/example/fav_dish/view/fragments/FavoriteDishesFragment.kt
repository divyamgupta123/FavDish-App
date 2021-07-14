package com.example.fav_dish.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.fav_dish.application.FavDishApplication
import com.example.fav_dish.databinding.FragmentFavoriteDishesBinding
import com.example.fav_dish.model.entities.FavDish
import com.example.fav_dish.view.activities.MainActivity
import com.example.fav_dish.view.adapters.FavDishAdapter
import com.example.fav_dish.viewmodel.FavDishViewModel
import com.example.fav_dish.viewmodel.FavDishViewModelFactory

class FavoriteDishesFragment : Fragment() {

    private var _binding: FragmentFavoriteDishesBinding? = null

    private val mFavDishViewModel: FavDishViewModel by viewModels {
        FavDishViewModelFactory((requireActivity().application as FavDishApplication).repository)
    }

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentFavoriteDishesBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mFavDishViewModel.favDishesList.observe(viewLifecycleOwner) { dishes ->
            dishes.let {

                binding.rvFavoriteDishesList.layoutManager = GridLayoutManager(requireActivity(), 2)

                val adapter = FavDishAdapter(this)
                binding.rvFavoriteDishesList.adapter = adapter

                if (it.isNotEmpty()) {
                    binding.rvFavoriteDishesList.visibility = View.VISIBLE
                    binding.tvNoFavoriteDishesAvailable.visibility = View.GONE
                    adapter.dishesList(it)
                } else {
                    binding.rvFavoriteDishesList.visibility = View.GONE
                    binding.tvNoFavoriteDishesAvailable.visibility = View.VISIBLE
                }
            }
        }
    }

    fun dishDetails(favDish: FavDish) {
        if (requireActivity() is MainActivity) {
            (activity as MainActivity?)!!.hideBottomNavigationView()
        }

        findNavController().navigate(
            FavoriteDishesFragmentDirections.actionFavoriteDishesToDishDetails(
                favDish
            )
        )
    }

    override fun onResume() {
        super.onResume()
        if (requireActivity() is MainActivity) {
            (activity as MainActivity?)!!.showBottomNavigationView()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}