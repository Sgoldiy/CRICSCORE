package com.cricpeak.scorelive.ui

import android.app.Activity
import androidx.compose.foundation.Image
import com.cricpeak.scorelive.R
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cricpeak.scorelive.billing.BillingViewModel

@Composable
fun PurchaseScreen(
    onClose: () -> Unit,
    billingViewModel: BillingViewModel = viewModel()
) {
    var selectedTabIndex by remember { mutableIntStateOf(2) }

    val plans = listOf("1 Month", "6 Month", "1 Year", "Lifetime")
    val productIds = listOf(
        "monthly_sub",
        "half_yearly_sub",
        "yearly_sub",
        "lifetime_access"
    )
    val goldGradient = Brush.radialGradient(
        colors = listOf(
            Color(0xFFFFD700).copy(alpha = 0.25f),
            Color.Transparent
        ),
        center = Offset(600f, -100f),
        radius = 1200f
    )
    val fallbackPrices = listOf("₹199", "₹399", "₹599", "₹2999")

    val products by billingViewModel.products.collectAsState()
    val isPremium by billingViewModel.isPremium.collectAsState()
    val context = LocalContext.current

    val selectedProduct = products.find {
        it.productId == productIds[selectedTabIndex]
    }

    val displayPrice = selectedProduct?.let {
        if (it.productId == "lifetime_access") {
            it.oneTimePurchaseOfferDetails?.formattedPrice
        } else {
            it.subscriptionOfferDetails
                ?.firstOrNull()
                ?.pricingPhases
                ?.pricingPhaseList
                ?.firstOrNull()
                ?.formattedPrice
        }
    } ?: fallbackPrices[selectedTabIndex]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(goldGradient)
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // HEADER
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {

                Text(
                    text = "Get Premium",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = onClose,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // IMAGE
            Image(
                painter = painterResource(id = R.drawable.ic_gold_man),
                contentDescription = null,
                modifier = Modifier
                    .size(220.dp)
                    .padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // TITLE
            Text(
                text = if (isPremium) "Premium Unlocked" else "Unlock Premium",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            // PLAN SELECTOR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(48.dp)
                    .background(Color(0xFF1A1A1A), RoundedCornerShape(24.dp))
                    .padding(4.dp)
            ) {

                plans.forEachIndexed { index, plan ->
                    val isSelected = selectedTabIndex == index

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isSelected) Color(0xFFFFD700).copy(alpha = 0.2f)
                                else Color.Transparent
                            )
                            .clickable { selectedTabIndex = index },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = plan,
                            color = if (isSelected) Color(0xFFFFD700) else Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // FEATURES
            Column(
                modifier = Modifier.padding(horizontal = 32.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                FeaturePoint("Ad-free experience")
                FeaturePoint("AI-powered fantasy team suggestions")
                FeaturePoint("Premium golden app theme")
            }

            Spacer(modifier = Modifier.height(32.dp))

            // PRICE
            Text(
                text = displayPrice,
                color = Color.White,
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            // BUTTON
            Button(
                onClick = {
                    if (selectedProduct != null) {
                        billingViewModel.buyProduct(
                            context as Activity,
                            selectedProduct
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFC107)
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(
                    text = "Go Premium",
                    color = Color.Black,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // PREMIUM STATE
            if (isPremium) {
                Text(
                    text = "You already have Premium",
                    color = Color(0xFFFFD700),
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun FeaturePoint(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = Color(0xFFFFD700),
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = text, color = Color.White, fontSize = 14.sp)
    }
}
