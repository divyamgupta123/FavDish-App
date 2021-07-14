package com.example.fav_dish.view.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fav_dish.R
import com.example.fav_dish.application.FavDishApplication
import com.example.fav_dish.databinding.DialogCustomListBinding
import com.example.fav_dish.databinding.FragmentAllDishesBinding
import com.example.fav_dish.model.entities.FavDish
import com.example.fav_dish.utils.Constants
import com.example.fav_dish.view.activities.AddUpdateDishActivity
import com.example.fav_dish.view.activities.MainActivity
import com.example.fav_dish.view.adapters.CustomListItemAdapter
import com.example.fav_dish.view.adapters.FavDishAdapter
import com.example.fav_dish.viewmodel.FavDishViewModel
import com.example.fav_dish.viewmodel.FavDishViewModelFactory

class AllDishesFragment : Fragment() {

    private var _binding: FragmentAllDishesBinding? = null

    private lateinit var mFavDishAdapter: FavDishAdapter

    private lateinit var mCustomListDialog: Dialog

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
        _binding = FragmentAllDishesBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvDishesList.layoutManager = GridLayoutManager(requireActivity(), 2)
        mFavDishAdapter = FavDishAdapter(this@AllDishesFragment)
        binding.rvDishesList.adapter = mFavDishAdapter

        mFavDishViewModel.allDishesList.observe(viewLifecycleOwner) { dishes ->
            dishes.let {

                if (dishes.isNotEmpty()) {
                    binding.rvDishesList.visibility = View.VISIBLE
                    binding.tvNoDishesAddedYet.visibility = View.GONE
                    mFavDishAdapter.dishesList(it)
                } else {
                    binding.rvDishesList.visibility = View.GONE
                    binding.tvNoDishesAddedYet.visibility = View.VISIBLE
                }

            }
        }
    }

    fun deleteDish(dish: FavDish) {
        val builder = AlertDialog.Builder(requireActivity())
        builder.setTitle(resources.getString(R.string.title_delete_dish))
        builder.setMessage(resources.getString(R.string.msg_delete_dish_dialog, dish.title))
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        builder.setPositiveButton(resources.getString(R.string.lbl_yes)) { dialogInterface, _ ->
            mFavDishViewModel.delete(dish)
            dialogInterface.dismiss()
        }

        builder.setNegativeButton(resources.getString(R.string.lbl_no)) { dialogInterface, _ ->
            dialogInterface.dismiss()
        }

        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    fun dishDetails(favDish: FavDish) {
        findNavController().navigate(
            AllDishesFragmentDirections.actionAllDishesToDishDetails(
                favDish
            )
        )

        if (requireActivity() is MainActivity) {
            (activity as MainActivity?)?.hideBottomNavigationView()
        }
    }

    fun filterDishListDialog() {
        mCustomListDialog = Dialog(requireActivity())
        val binding: DialogCustomListBinding = DialogCustomListBinding.inflate(layoutInflater)
        mCustomListDialog.setContentView(binding.root)
        binding.tvTitle.text = resources.getString(R.string.title_select_item_to_filter)
        val dishTypes = Constants.dishTypes()
        dishTypes.add(0, Constants.ALL_ITEMS)
        binding.rvList.layoutManager = LinearLayoutManager(requireActivity())
        val adapter =
            CustomListItemAdapter(requireActivity(), this, dishTypes, Constants.FILTER_SELECTION)
        binding.rvList.adapter = adapter
        mCustomListDialog.show()
    }

    override fun onResume() {
        super.onResume()
        if (requireActivity() is MainActivity) {
            (activity as MainActivity?)?.showBottomNavigationView()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_all_dishes, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_add_dish -> {
                startActivity(Intent(requireActivity(), AddUpdateDishActivity::class.java))
                return true
            }
            R.id.action_filter_dishes -> {
                filterDishListDialog()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun filterSelection(filterItemSelection: String) {
        mCustomListDialog.dismiss()
        if (filterItemSelection == Constants.ALL_ITEMS) {
            mFavDishViewModel.allDishesList.observe(viewLifecycleOwner) { dishes ->
                dishes.let {

                    if (dishes.isNotEmpty()) {
                        binding.rvDishesList.visibility = View.VISIBLE
                        binding.tvNoDishesAddedYet.visibility = View.GONE
                        mFavDishAdapter.dishesList(it)
                    } else {
                        binding.rvDishesList.visibility = View.GONE
                        binding.tvNoDishesAddedYet.visibility = View.VISIBLE
                    }

                }
            }
        } else {
            mFavDishViewModel.getFilteredList(filterItemSelection)
                .observe(viewLifecycleOwner) { dishes ->
                    dishes.let {
                        if (it.isNotEmpty()) {
                            binding.rvDishesList.visibility = View.VISIBLE
                            binding.tvNoDishesAddedYet.visibility = View.GONE
                            mFavDishAdapter.dishesList(it)
                        } else {
                            binding.rvDishesList.visibility = View.GONE
                            binding.tvNoDishesAddedYet.visibility = View.VISIBLE
                        }
                    }
                }
        }
    }

}