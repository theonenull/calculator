package data

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.toMutableStateList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import logic.evaluateExpression
import javax.script.ScriptEngineManager

class ViewModelState {
    private val scope = CoroutineScope(Job())
    private val scope2 = CoroutineScope(Job())
    val stringForCalculator = MutableStateFlow("")
    val scrollableState = ScrollState(initial = 0)
    val result = MutableStateFlow<Result>(Result.Null())
    val withX = stringForCalculator
        .map {
            it.indexOf("x") != -1
        }
        .stateIn(
            scope2,
            SharingStarted.Eagerly,
            false
        )
    val chartStart = MutableStateFlow("")
    val chartEnd = MutableStateFlow("")
    val charResult = chartEnd
        .combine(chartStart){ end , start ->
            if( end.isEmpty() || start.isEmpty() ){
                return@combine "��Χ����Ϊ��"
            }
            try {
                val endInt = chartEnd.value.toInt()
                val startInt = chartStart.value.toInt()
                if(startInt > endInt){
                    return@combine "����Ҫ���ڿ�ʼ"
                }
                if(endInt-startInt > 100){
                    return@combine "��Χ���󣬷�Χ��100����"
                }
            }catch (e:Exception){
                return@combine "��ʽ���󣬱���Ϊ����"
            }
            return@combine "��ʽ��ȷ"
        }.stateIn(
            scope,
            SharingStarted.Eagerly,
            ""
        )
    val dataForDraw = MutableStateFlow<MutableList<ChartItem>>(mutableStateListOf())
    val dialogState = MutableStateFlow<String?>(null)
    fun subString(){
        stringForCalculator.let { stringMutableStateFlow ->
            if (stringMutableStateFlow.value.isNotEmpty()) {
                stringMutableStateFlow.update {
                    val data = it
                    if(data.substring(0, data.length - 1).isEmpty()){
                        result.value = Result.Null()
                    }
                    it.substring(0, it.length - 1)
                }
            }
        }
    }

    fun addSting(new:String){
        stringForCalculator.let { stringMutableStateFlow ->
            if((stringMutableStateFlow.value+new).length<50){
                stringMutableStateFlow.update {
                    it + new
                }
                if(result.value !is Result.Loading){
                    result.value = Result.Loading()
                }
            }
        }
    }
    fun compute(){
        if(stringForCalculator.value.isEmpty() ){
            result.value = Result.Null()
            return
        }
        if(stringForCalculator.value.indexOf("x")!=-1){
            result.value = Result.WithX()
            return
        }
        val sanitizedExpression = stringForCalculator.value.replace(" ", "") // ɾ���ո�
        val theResult = try {
            val data = evaluateExpression(sanitizedExpression)
            result.value = Result.Success(data.toString())
        } catch (e: Exception) {
            result.value = Result.Error(Throwable(e))
        }
    }

    fun updateChartStart(string:String){
        scope.launch(Dispatchers.IO) {
            chartStart.emit(string)
//            chartEnd.emit(chartEnd.value)
        }
    }

    fun updateChartEnd(string:String){
        scope.launch(Dispatchers.IO) {
            chartEnd.emit(string)
//            chartStart.emit(chartStart.value)
        }
    }

    fun updateDataForCharMain(){
        if(charResult.value!="��ʽ��ȷ"){
            dialogState.value = "��Χ����"
            return
        }
        val range = (chartStart.value.toInt()..chartEnd.value.toInt())
        val list = mutableListOf<Double>()
        for( item in range){
            try {
                val sanitizedExpression =
                    if(item<0){
                        stringForCalculator.value.replace("x","(0${item.toString()})")
                    }else{
                        stringForCalculator.value.replace("x", item.toString())
                    }
                val data = evaluateExpression(sanitizedExpression)
                list.add(data)
            }
            catch (e:Exception){
                dialogState.value = "���ʽ����"
                return
            }
        }
        val ymax = list.max()
        val ymin = list.min()
        val xmax = range.toList().max().toDouble()
        val xmin = range.toList().min().toDouble()
        dataForDraw.value = list.mapIndexed { index, d ->
            ChartItem(
                x = range.elementAt(index),
                y = d,
                xPercent = (range.elementAt(index).toDouble()-xmin)/(xmax - xmin),
                yPercent = (d.toDouble()-ymin)/(ymax - ymin)
            )
        }.toMutableStateList()
    }

}

data class ChartItem(
    val x : Int,
    val y :Double,
    val xPercent : Double,
    val yPercent : Double
)