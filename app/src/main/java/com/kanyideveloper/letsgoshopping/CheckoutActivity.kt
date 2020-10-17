package com.kanyideveloper.letsgoshopping

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_checkout.*
import kotlinx.android.synthetic.main.cart_items_row.*


class CheckoutActivity : AppCompatActivity(), ItemClickListener {

    private lateinit var mDatabase: FirebaseDatabase
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mReference: DatabaseReference
    private var cartList : ArrayList<CartItem>? = null
    private lateinit var adapter : CartItemsAdapter

    private val sharedPrefFile = "kotlinsharedpreference"
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        sharedPreferences = this.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)

        mDatabase = FirebaseDatabase.getInstance()

        cartList = arrayListOf()

        mReference = mDatabase.getReference("cart_items")

        mReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot : DataSnapshot) {
                if (snapshot.exists()) {
                    for (i in snapshot.children) {
                        val item = i.getValue(CartItem::class.java)
                        cartList!!.add(item!!)
                    }

                    var sum = 0.0
                    var vat = 0.0
                    var total = 0.0
                    for (itm : CartItem in cartList!!) {
                        sum += itm.itemPrice.toDouble()
                        vat += itm.vat.toDouble()
                        total = sum + vat
                    }
                    subtotal_value.text = sum.toString()
                    vat_value.text = vat.toString()

                    total_value.text = total.toString()

                    //Initialise my adapter
                    //adapter = CartItemsAdapter(applicationContext, cartList!!, this@CheckoutActivity)
                    //mRecyclerView.adapter = adapter

                    initialiseRecycler()
                }
            }

            override fun onCancelled(error : DatabaseError) {
            }
        })
    }

    override fun increaseToCart(item : CartItem, position : Int) {
        adapter.notifyItemChanged(position)
    }

    override fun decreaseFromCart(item : CartItem, position : Int) {
        item.counter -= 1
        adapter.notifyItemChanged(position)
    }

    override fun deleteFromCart(item : CartItem, position : Int) {

        val mQuery : Query = mReference.orderByChild("itemName").equalTo(item.itemName)
        mQuery.addListenerForSingleValueEvent( object : ValueEventListener{
            override fun onDataChange(snapshot : DataSnapshot) {
                for(snapshot in snapshot.children){
                    snapshot.ref.removeValue()
                    removeItem(position)
                    adapter.clear()
                    decrementCounter()
                }
            }
            override fun onCancelled(error : DatabaseError) {
            }
        })
    }

    private fun incrementCounter(){
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putInt(Utils.counter.toString(), sharedPreferences.getInt(Utils.counter.toString(), 0) + 1)
        editor.apply()
        editor.commit()
        val sharedIdValue = sharedPreferences.getInt(Utils.counter.toString(), 0)
        items_num.text = sharedIdValue.toString()
    }

    private fun decrementCounter(){
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putInt(Utils.counter.toString(), sharedPreferences.getInt(Utils.counter.toString(), 0) - 1)
        editor.apply()
        editor.commit()
        val sharedIdValue = sharedPreferences.getInt(Utils.counter.toString(), 0)
        items_num.text = sharedIdValue.toString()
    }

    private fun removeItem(position : Int){
        cartList?.removeAt(position)
        adapter.notifyItemRemoved(position)
    }

    private fun initialiseRecycler(){
        mRecyclerView = findViewById(R.id.cart_recycler)
        mRecyclerView.hasFixedSize()
        mRecyclerView.layoutManager = LinearLayoutManager(applicationContext)
        adapter = CartItemsAdapter(applicationContext, cartList!!, this@CheckoutActivity)
        mRecyclerView.adapter = adapter
    }
}