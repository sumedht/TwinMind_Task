package com.sumedh.wayfair.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.sumedh.wayfair.domain.model.Product
import androidx.compose.ui.unit.dp

@Composable
fun ProductListItem(
    product: Product
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(20.dp)
    ) {
        Text(
            text = product.name,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.body1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = product.tagline,
            modifier = Modifier.padding(vertical = 8.dp),
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.body1,
            overflow = TextOverflow.Ellipsis
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                text = product.date,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.body1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.width(20.dp))

            Text(
                text = "Rating: "+product.rating.toString(),
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.body1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}