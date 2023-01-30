package com.example.disheatapp.view.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.disheatapp.R

import com.example.disheatapp.application.FavDishApplication
import com.example.disheatapp.databinding.FragmentFavoritesDishesBinding
import com.example.disheatapp.model.entities.FavDish
import com.example.disheatapp.view.activities.MainActivity
import com.example.disheatapp.view.adapters.FavDishAdapter
import com.example.disheatapp.viewmodel.FavDishViewModel
import com.example.disheatapp.viewmodel.FavDishViewModelFactory

class FavoriteDishesFragment : Fragment() {

    private var _binding: FragmentFavoritesDishesBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private var stateFavoriteIcon = false



    private val mFavDishViewModel: FavDishViewModel by viewModels {
        FavDishViewModelFactory(((requireActivity().application) as FavDishApplication).repository)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        _binding = FragmentFavoritesDishesBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.rvFavoriteDishesList?.layoutManager = GridLayoutManager(requireActivity(), 2)
        val adapter = FavDishAdapter(this@FavoriteDishesFragment)
        binding?.rvFavoriteDishesList?.adapter = adapter


        mFavDishViewModel.favoriteDishes.observe(viewLifecycleOwner){
                dishes ->
            dishes.let{

                if(it.isNotEmpty()){
                   /* for (dish in it) {
                        Log.i("Dish Title", "${dish.id} :: ${dish.title}")
                    }*/
                    binding?.rvFavoriteDishesList?.visibility   = View.VISIBLE

                    binding?.tvNoFavoriteDishesAddedYet?.visibility   = View.GONE
                    adapter.dishesList(it)

                } else{
                    binding?.rvFavoriteDishesList?.visibility  = View.GONE
                    binding?.tvNoFavoriteDishesAddedYet?.visibility = View.GONE

                }
            }
        }
    }

    fun dishDetails(favDish: FavDish){

        findNavController().navigate(FavoriteDishesFragmentDirections.actionNavigationFavoriteDishesToNavigationDishDetails(
            favDish, stateFavoriteIcon
        ))
        if(requireActivity() is MainActivity) {
            (activity as MainActivity).hideBottomNavigationView()
        }

    }



    override fun onResume() {
        super.onResume()
        if(requireActivity() is MainActivity) {
            (activity as MainActivity).showBottomNavigationView()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}