package com.femastudios.esr.datastruct

import java.io.IOException
import java.util.*

class HTMLTemplate<T>(
		templateResourceName: String,
		parameters: List<(T) -> Any?>
) {
	private val parameters: List<(T) -> Any?>
	private val template: String

	@SafeVarargs
	constructor(templateResourceName: String, vararg parameters: (T) -> Any?) : this(templateResourceName, Arrays.asList(*parameters)) {
	}

	init {
		this.parameters = ArrayList(parameters)
		this.template = readTemplate(templateResourceName)
	}

	fun getParameters(): List<(T) -> Any?> {
		return Collections.unmodifiableList(parameters)
	}

	@Throws(IOException::class)
	fun apply(subject: T): String {
		val arguments = arrayOfNulls<Any>(parameters.size)
		for (i in parameters.indices) {
			arguments[i] = parameters[i](subject)
		}
		return String.format(template, *arguments)
	}

	private fun readTemplate(templateResourceName: String): String {
		val classLoader = HTMLTemplate::class.java.classLoader
		val stream = classLoader.getResourceAsStream(templateResourceName)
		val s = Scanner(stream).useDelimiter("\\A")
		return if (s.hasNext()) {
			s.next().replace("(?s)<!--.*-->".toRegex(), "")
		} else {
			""
		}
	}

	@Throws(IOException::class)
	fun applyMultiple(subjects: Iterable<T>): String {
		val ret = StringBuilder()
		for (subject in subjects) {
			ret.append(apply(subject))
		}
		return ret.toString()
	}
}
