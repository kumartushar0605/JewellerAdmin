package com.example.gehnamall;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gehnamall.Product;
import com.example.gehnamall.ProductImageAdapter;
import com.example.gehnamall.R;


import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<Product> productList;
    private Context context;
    private ProductDeleteListener deleteListener;

    public ProductAdapter(List<Product> productList, Context context, ProductDeleteListener deleteListener) {
        this.productList = productList;
        this.context = context;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.product_item, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);

        // Set up image RecyclerView
        ProductImageAdapter imageAdapter = new ProductImageAdapter(product.getImageUrls(), context);
        holder.recyclerViewProductImages.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        holder.recyclerViewProductImages.setAdapter(imageAdapter);
    String Category = null;
    String Subcategory = null;
    String Occasion=null;
    String Gifting=null;
        String Soulmate=null;
    String Weight=null;
    String Wastage=null;
    String tagNumber=null;
        String  size=null;
        String  length=null;
       String karat=null;
        // Set product details
        if (product.getCategory() == null) {
            Category = "";
        }else{
            Category=product.getCategory();
        }
        if (product.getSubcategory() == null) {
           Subcategory ="";
        }else{
            Subcategory= product.getSubcategory();
        }
        if (product.getOccasion() == null) {
           Occasion = "";
        }else{
            Occasion= product.getOccasion();
        }
        if (product.getGifting() == null) {
          Gifting = "";
        }else{
            Gifting= product.getGifting();
        }
        if (product.getWeight() == null) {
           Weight = "";
        }else{
            Weight= product.getWeight();
        }
        if (product.getWastage() == null) {
          Wastage = "";
        }else{
            Wastage=product.getWastage();
        }
        if (product.getTagNumber() == null) {
           tagNumber = "";
        }else{
            tagNumber= product.getTagNumber();
        }
        if (product.getSize() == null) {
           size = "";
        }else{
            size=product.getSize();
        }
        if (product.getLength() == null) {
           length = "";
        }else{
            length=product.getLength();
        }
        if (product.getKarat() == null) {
           karat = "";
        }else{
            karat=product.getKarat();
        }
        if (product.getSoulmate() == null) {
            Soulmate= product.getSoulmate();
        }
        holder.textProductDetails.setText(
                "Category: " + Category + "\n" +
                        "Subcategory: " + Subcategory + "\n" +
                        "Soulmate: " +  Soulmate+ "\n" +
                          Occasion + "\n" +
                         Gifting +"\n" +
                        karat +"\n" +
                        Weight +"\n" +
                        Wastage +"\n" +
                        tagNumber +"\n" +
                        size +"\n" +
                        length



        );

        // Set delete button
        holder.btnDeleteProduct.setOnClickListener(v -> deleteListener.onDelete(product.getId(), position));
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        RecyclerView recyclerViewProductImages;
        TextView textProductDetails;
        Button btnDeleteProduct;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            recyclerViewProductImages = itemView.findViewById(R.id.recyclerViewProductImages);
            textProductDetails = itemView.findViewById(R.id.textProductDetails);
            btnDeleteProduct = itemView.findViewById(R.id.btnDeleteProduct);
        }
    }

    public interface ProductDeleteListener {
        void onDelete(String productId, int position);
    }
}
