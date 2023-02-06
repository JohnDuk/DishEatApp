package com.example.disheatapp.view.fragments

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.disheatapp.R
import com.example.disheatapp.application.FavDishApplication

import com.example.disheatapp.databinding.FragmentRandomDishBinding
import com.example.disheatapp.model.entities.FavDish
import com.example.disheatapp.model.entities.RandomDish
import com.example.disheatapp.utils.Constants
import com.example.disheatapp.viewmodel.FavDishViewModel
import com.example.disheatapp.viewmodel.FavDishViewModelFactory
import com.example.disheatapp.viewmodel.NotificationsViewModel
import com.example.disheatapp.viewmodel.RandomDishViewModel

class RandomDishFragment : Fragment() {

    private var _binding: FragmentRandomDishBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val mBinding get() = _binding!!
    private lateinit var mRandomDishViewModel: RandomDishViewModel

    private var mProgressDialog: Dialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        _binding = FragmentRandomDishBinding.inflate(inflater, container, false)
        val root: View = mBinding.root
        return root
    }

    private fun showCustomProgressDialog(){
        mProgressDialog = Dialog(requireActivity())
        mProgressDialog?.let {
            it.setContentView(R.layout.dialog_custom_progress)
            it.show()
        }
    }

    private fun hideCustomProgressDialog(){
        mProgressDialog?.let {
            it.dismiss()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the ViewModel variable.
        mRandomDishViewModel =
            ViewModelProvider(this).get(RandomDishViewModel::class.java)

        // TODO Step 3: Call the function to get the response from API.
        // START
        mRandomDishViewModel.getRandomDishFromAPI()
        // END

        // TODO Step 5: Call the observer function.
        // START
        randomDishViewModelObserver()
        // END

        mBinding.srlRandomDish.setOnRefreshListener {
            mRandomDishViewModel.getRandomDishFromAPI()

        }

    }

    private fun randomDishViewModelObserver() {

        mRandomDishViewModel.randomDishResponse.observe(
            viewLifecycleOwner,
            Observer { randomDishResponse ->
                randomDishResponse?.let {
                    Log.i("Random Dish Response", "$randomDishResponse.recipes[0]")
                    if(mBinding.srlRandomDish.isRefreshing){
                        mBinding.srlRandomDish.isRefreshing = false

                    }
                    setRandomDishResponseInUI(randomDishResponse.recipes[0] )
                }
            })

        mRandomDishViewModel.randomDishLoadingError.observe(
            viewLifecycleOwner,
            Observer { dataError ->
                dataError?.let {
                    Log.i("Random Dish API Error", "$dataError")
                    if(mBinding.srlRandomDish.isRefreshing){
                        mBinding.srlRandomDish.isRefreshing = false

                    }
                }
            })

        mRandomDishViewModel.loadRandomDish.observe(viewLifecycleOwner, Observer { loadRandomDish ->
            loadRandomDish?.let {
                Log.i("Random Dish Loading", "$loadRandomDish")

                if(loadRandomDish && !mBinding!!.srlRandomDish.isRefreshing) {
                    showCustomProgressDialog()
                } else {
                    hideCustomProgressDialog()
                }
            }
        })
    }

    // TODO Step 1: Create a method to populate the API response in the UI.
    // START
    /**
     * A method to populate the API response in the UI.
     *
     * @param recipe - Data model class of the API response with filled data.
     */
    private fun setRandomDishResponseInUI(recipe: RandomDish.Recipe) {

        // Load the dish image in the ImageView.
        Glide.with(requireActivity())
            .load(recipe.image)
            .centerCrop()
            .into(mBinding!!.ivDishImage)

        mBinding!!.tvTitle.text = recipe.title

        // Default Dish Type
        var dishType: String = "other"

        if (recipe.dishTypes.isNotEmpty()) {
            dishType = recipe.dishTypes[0]
            mBinding!!.tvType.text = dishType
        }

        // There is not category params present in the response so we will define it as Other.
        mBinding!!.tvCategory.text = "Other"

        var ingredients = ""
        for (value in recipe.extendedIngredients) {

            if (ingredients.isEmpty()) {
                ingredients = value.original
            } else {
                ingredients = ingredients + ", \n" + value.original
            }
        }

        mBinding!!.tvIngredients.text = ingredients

        // The instruction or you can say the Cooking direction text is in the HTML format so we will you the fromHtml to populate it in the TextView.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mBinding!!.tvCookingDirection.text = Html.fromHtml(
                recipe.instructions,
                Html.FROM_HTML_MODE_COMPACT
            )
        } else {
            @Suppress("DEPRECATION")
            mBinding!!.tvCookingDirection.text = Html.fromHtml(recipe.instructions)
        }

        mBinding.ivFavoriteDish.setImageDrawable(
            ContextCompat.getDrawable(
                requireActivity(),
                R.drawable.ic_favorite_unselected
            )
        )

        var addedToFavorites = false





        mBinding!!.tvCookingTime.text =
            resources.getString(
                R.string.lbl_estimate_cooking_time,
                recipe.readyInMinutes.toString()
            )

        // TODO step 6: Assign the click event to the Favorite Button and add the dish details to the local database if user click on it.
        // START
        mBinding!!.ivFavoriteDish.setOnClickListener {

            if(addedToFavorites) {
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
                // END

                // TODO Step 8: Create an instance of FavDishViewModel class and call insert function and pass the required details.
                // START
                val mFavDishViewModel: FavDishViewModel by viewModels {
                    FavDishViewModelFactory((requireActivity().application as FavDishApplication).repository)
                }

                mFavDishViewModel.insert(randomDishDetails)
                // END
                addedToFavorites = true

                // TODO Step 9: Once the dish is inserted you can acknowledge user by Toast message as below and also update the favorite image by selected.
                // START
                mBinding!!.ivFavoriteDish.setImageDrawable(
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

            // TODO Step 7: Create a instance of FavDish data model class and fill it with required information from the API response.
            // START

            // END
        }
        // END
    }
    // END



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}