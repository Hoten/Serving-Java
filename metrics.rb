def doMetrics(dir, extensions)
  result = { loc: 0, dir: dir, extensions: extensions }
  extensions.each { |ext|
    Dir.glob("#{dir}/**/*.#{ext}") { |file_path|
      File.open(file_path) { |file|
        result[:loc] += file.readlines.size
      }
    }
  }
  result
end

main_metrics =  doMetrics 'src/main', ['java', 'cs']
test_metrics =  doMetrics 'src/test', ['java', 'cs']

puts main_metrics
puts test_metrics
puts main_metrics[:loc] + test_metrics[:loc]
