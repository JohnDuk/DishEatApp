package com.example.disheatapp.view.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.disheatapp.R
import com.example.disheatapp.application.FavDishApplication
import com.example.disheatapp.databinding.DialogCustomListBinding
import com.example.disheatapp.databinding.FragmentAllDishesBinding
import com.example.disheatapp.model.entities.FavDish
import com.example.disheatapp.utils.Constants
import com.example.disheatapp.view.activities.AddUpdateDishActivity
import com.example.disheatapp.view.activities.MainActivity
import com.example.disheatapp.view.adapters.CustomListItemAdapter
import com.example.disheatapp.view.adapters.FavDishAdapter
import com.example.disheatapp.viewmodel.FavDishViewModel
import com.example.disheatapp.viewmodel.FavDishViewModelFactory

import com.example.disheatapp.viewmodel.HomeViewModel

class AllDishesFragment : Fragment(), MenuProvider {

    private var mBinding: FragmentAllDishesBinding? = null


    // This property is only valid between onCreateView and
    // onDestroyView.

    private val binding get() = mBinding!!
    private var stateFavoriteIcon = true


    private val mFavDishViewModel : FavDishViewModel by viewModels {
        FavDishViewModelFactory((requireActivity().application as FavDishApplication).repository)
    }


    private lateinit var mFavDishAdapter: FavDishAdapter
    private lateinit var mCustomListDialog: Dialog



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentAllDishesBinding.inflate(inflater, container, false)

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        return  mBinding!!.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding?.rvDishesList?.layoutManager = GridLayoutManager(requireActivity(), 2)
        mFavDishAdapter = FavDishAdapter(this@AllDishesFragment)
        mBinding?.rvDishesList?.adapter =  mFavDishAdapter

        mFavDishViewModel.allDishesList.observe(viewLifecycleOwner){
                dishes ->
            dishes.let{
               /* for (item in it) {
                    Log.i("Dish Title", "${item.id} :: ${item.title} ${item.favoriteDish}")
                }
*/
                if(it.isNotEmpty()){
                    mBinding?.rvDishesList?.visibility   = View.VISIBLE
                    mBinding?.tvNoDishesAddedYet?.visibility   = View.GONE
                    mFavDishAdapter.dishesList(it)

                } else{
                    mBinding?.rvDishesList?.visibility  = View.GONE
                    mBinding?.tvNoDishesAddedYet?.visibility = View.GONE

                }
            }
        }

    }

    fun dishDetails(favDish: FavDish){
        findNavController().navigate(AllDishesFragmentDirections.actionAllDishesToDishDetails(
            favDish, stateFavoriteIcon
        ))
        if(requireActivity() is MainActivity) {
            (activity as MainActivity).hideBottomNavigationView()
        }

    }

    fun deleteDish(dish: FavDish){
        val builder = AlertDialog.Builder(requireActivity())
        builder.setTitle(resources.getString(R.string.title_delete_dish))
        builder.setMessage(resources.getString(R.string.msg_delete_dish_dialog, dish.title))
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        builder.setPositiveButton(resources.getString(R.string.lbl_yes)) { dialogInterface, _ ->
            mFavDishViewModel.delete(dish)
            dialogInterface.dismiss()
        }

        builder.setNegativeButton(resources.getString(R.string.lbl_no)) { dialogInterface,_ ->
            dialogInterface.dismiss()

        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    private fun filterDishesListDialog(){
        mCustomListDialog = Dialog(requireActivity())
        val binding: DialogCustomListBinding = DialogCustomListBinding.inflate(layoutInflater)
        mCustomListDialog.setContentView(binding.root)
        binding.tvTitle.text = resources.getString(R.string.title_select_item_to_filter)
        val dishTypes = Constants.dishTypes()
        dishTypes.add(0, Constants.ALL_ITEMS)
        binding.rvList.layoutManager = LinearLayoutManager(requireActivity())
        val adapter = CustomListItemAdapter(requireActivity(), this@AllDishesFragment, dishTypes, Constants.FILTER_SELECTION)
        binding.rvList.adapter = adapter
        mCustomListDialog.show()

    }

    override fun onResume() {
        super.onResume()
        if(requireActivity() is MainActivity) {
            (activity as MainActivity).showBottomNavigationView()
        }

    }


    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_all_dishes, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when(menuItem.itemId){
            R.id.action_add_dish -> {
                startActivity(Intent(requireActivity(), AddUpdateDishActivity::class.java))
                return  true
            }

            R.id.action_filter_dishes -> {
                filterDishesListDialog()
                return true
            }

        }
        return super.onOptionsItemSelected(menuItem)
    }

    fun filterSelection(filterItemSelection: String) {

        mCustomListDialog.dismiss()

        Log.i("Filter Selection", filterItemSelection)

        if (filterItemSelection == Constants.ALL_ITEMS) {
            mFavDishViewModel.allDishesList.observe(viewLifecycleOwner) { dishes ->
                dishes.let {
                    if (it.isNotEmpty()) {

                        mBinding?.rvDishesList?.visibility = View.VISIBLE
                        mBinding?.tvNoDishesAddedYet?.visibility = View.GONE
                        mFavDishAdapter.dishesList(it)

                    } else {

                        mBinding?.rvDishesList?.visibility = View.GONE
                        mBinding?.tvNoDishesAddedYet?.visibility = View.VISIBLE
                    }
                }
            }
        }else {

            // TODO Step 4: Remove the log and replace it with filtered list as below.
            // START
            mFavDishViewModel.getFilteredList(filterItemSelection)
                .observe(viewLifecycleOwner) { dishes ->
                    dishes.let {
                        if (it.isNotEmpty()) {

                            mBinding?.rvDishesList?.visibility = View.VISIBLE
                            mBinding?.tvNoDishesAddedYet?.visibility = View.GONE
                            mFavDishAdapter.dishesList(it)

                        } else {

                            mBinding?.rvDishesList?.visibility = View.GONE
                            mBinding?.tvNoDishesAddedYet?.visibility = View.VISIBLE
                        }
                    }
                }

    }
    }



}