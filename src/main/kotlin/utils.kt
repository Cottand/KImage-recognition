import arrow.syntax.collections.tail
import java.io.File
import kotlinx.collections.immutable.PersistentList
import org.jetbrains.numkt.array
import org.jetbrains.numkt.core.KtNDArray
import org.jetbrains.numkt.zeros

/**
 * Computes the numerical gradient of receiver at [x]
 */
fun ((Matrix) -> Real).numericalGradient(x: Matrix): Matrix {
  val fx = this(x)
  val grad = zeros<Real>(*x.shape)
  val h = HyperParams.gradientH

  var i = 0
  for (v in x) {
    assert(x[i] == v) // TODO remove

    val oldValue = x[i]
    x[i] = oldValue + h
    val fxh = this(x)

    x[i] = oldValue

    grad[i] = (fxh - fx) / h

    i++
  }
  return grad
}

inline fun <reified T : Any, reified S : Any> KtNDArray<T>.map(transform: (T) -> S): KtNDArray<S> {
  val ret = zeros<S>(*this.shape)
  for ((i, v) in this.iterator().withIndex()) {
    ret[i] = transform(v)
  }
  return ret
}

/**
 * Parses MNIST CSV file. Format is:
 * label, 1x1, .., 28x28
 * l, x0, .., x728
 */
fun parseCSVDataMNIST(): Set<Labelled> {
  val f = File("mnist_tran.csv")
  val lines = f.readLines(Charsets.UTF_8).tail()
  return lines.map { line ->
    val fields = line.split(',')
    val label = fields.first().toInt()
    assert(label in 0..9)
    val pixels = fields.tail().map { it.toInt().toDouble() }
    // TODO verify this is a 1 column vector
    label to array<Real>(listOf(pixels)).t
  }.toSet()
}

fun Collection<Labelled>.splitIntoBatches(batchSize: Int) =
  withIndex()
    .groupBy { (i, _) -> i / batchSize }
    .map { (_, v) -> v.map { it.value } }

inline fun <reified T : Number> KtNDArray<T>.withIndex() = this.iterator().withIndex()

inline fun <reified T : Number> KtNDArray<T>.toList() = this.iterator().asSequence().toList()

inline operator fun <reified T> T.plus(list: PersistentList<T>) = list.add(0, this)

inline fun <reified T : Number> List<KtNDArray<T>>.concatenateToMatrix() =
  org.jetbrains.numkt.concatenate(*this.toTypedArray())
