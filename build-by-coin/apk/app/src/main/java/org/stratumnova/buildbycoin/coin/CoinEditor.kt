package org.stratumnova.buildbycoin.coin
import android.app.Activity
import android.app.AlertDialog
import android.widget.*
import java.io.File

object CoinEditor {
 fun candidateDialog(activity:Activity,artifact:File,onSave:(String)->Unit){
  val box=LinearLayout(activity).apply{orientation=LinearLayout.VERTICAL;setPadding(24,8,24,8)}
  fun field(h:String)=EditText(activity).apply{hint=h}
  val id=field("Coin ID — UNRESOLVED");val type=field("Type");val domain=field("Domain");val role=field("Role");val category=field("Category");val summary=field("Summary")
  listOf(id,type,domain,role,category,summary).forEach{box.addView(it)}
  AlertDialog.Builder(activity).setTitle("Candidate Coin: "+artifact.name).setView(box)
   .setNegativeButton("CANCEL",null).setPositiveButton("SAVE CANDIDATE"){_,_->
    fun v(e:EditText)=e.text.toString().trim().ifBlank{"UNRESOLVED"}
    val json=CoinMint.candidate(artifact,CandidateFields(v(id),v(type),v(domain),v(role),v(category),v(summary)))
    onSave(json.toString(2))
   }.show()
 }
}