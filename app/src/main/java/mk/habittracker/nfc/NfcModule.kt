package mk.habittracker.nfc

import android.content.Context
import android.nfc.NfcAdapter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object NfcModule {
    @Provides
    fun provideNfcAdapter(
        @ApplicationContext ctx: Context,
    ): NfcAdapter? = NfcAdapter.getDefaultAdapter(ctx)
}
