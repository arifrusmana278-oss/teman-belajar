<script setup>
import { computed, nextTick, onMounted, onUnmounted, ref } from 'vue'
const API = import.meta.env.VITE_API_URL || '/api'
const API_ROOT = API.replace(/\/api\/?$/, '')
const authToken = ref(localStorage.getItem('tb_auth_token') || '')
const authReady = ref(false), authView = ref('login'), authBusy = ref(false), authError = ref('')
const account = ref(null)
const monitoringView = ref(false), adminStudents = ref([]), selectedMonitorStudent = ref(null), studentToDelete = ref(null), deletingStudentId = ref(null)
const adminLoading = ref(false), adminError = ref('')
const curriculumView = ref(false), curriculumLoading = ref(false), curriculumChecking = ref(false), curriculumError = ref('')
const curriculumRows = ref([]), curriculumSourceChecks = ref([])
const donationAdminView = ref(false), donationAdminLoading = ref(false), donationUploading = ref(false), donationAdminError = ref('')
const donationStatus = ref({ approved: false, pending: false }), donationFile = ref(null), donationNote = ref(''), donationMessage = ref('')
const donationOpen = ref(false)
const extracurricularView = ref(false), selectedClub = ref(null), selectedActivity = ref(null)
const selectedClubAnswer = ref(null), clubAnswerFeedback = ref(''), clubDraft = ref('')
const extracurricularState = ref({ interests: [], completed: {} })
const loginForm = ref({ identifier: '', password: '' })
const registerForm = ref({ name: '', username: '', email: '', grade: '1', password: '', confirmPassword: '' })
const gradeForm = ref('1'), gradeSaving = ref(false), gradeMessage = ref(''), gradeError = ref('')
const subjects = ref([]), lessons = ref([]), allSubjectLessons = ref([]), history = ref([])
const selectedGrade = ref(localStorage.getItem('tb_selected_grade') || '1')
const selectedNav = ref('home')
const loading = ref(true), error = ref(''), selectedSubject = ref(null), activeLesson = ref(null)
const questionsNote = ref('')
const questions = ref([]), answers = ref([]), questionIndex = ref(0), selectedOption = ref(null), feedback = ref(null)
const printPreview = ref(null), printLoading = ref(false), printError = ref('')
const score = ref(0), finished = ref(false), saving = ref(false), reviewMode = ref(false)
const hintVisible = ref(false), offlineMode = ref(!navigator.onLine)
const selectedMethod = ref(null), essayWorking = ref(''), essayAnswer = ref('')
const isEssay = computed(() => currentQuestion.value?.type === 'ESSAY')
const isAnswered = computed(() => !!answers.value[questionIndex.value])
const canCheck = computed(() => isEssay.value ? essayWorking.value.trim().length >= 3 && essayAnswer.value.trim().length > 0 : selectedOption.value !== null)
const methodTabs = computed(() => { const tabs = [{id:'long',label:'Cara panjang'},{id:'quick',label:'Cara cepat ⚡'}]; return activeLesson.value?.subjectName === 'Hitung Cepat' ? tabs.reverse() : tabs })
const activeMethod = computed(() => feedback.value?.quickMethod ? (selectedMethod.value ?? methodTabs.value[0].id) : 'long')
const textScale = ref(Number(localStorage.getItem('tb_text_scale') || 1))
const summaryMessage = ref(''), speechPlaying = ref(false)
const speechVoices = ref([]), selectedVoiceURI = ref(localStorage.getItem('tb_speech_voice') || '')
const studentName = ref(localStorage.getItem('tb_student_name') || 'Adik')
const studentAvatar = ref(localStorage.getItem('tb_student_avatar') || '👦')
const profileOpen = ref(false)
const studentAvatars = ['👧','👦','👩🏻‍🎓','👨🏻‍🎓','👩🏽‍🎓','👨🏽‍🎓','👩🏾‍🎓','👨🏾‍🎓','🧒','🧑‍🎓']
const philosophyIndex = ref(0)
const philosophies = [
  { title:'Trilogi pendidikan', text:'Ing ngarsa sung tulada · Ing madya mangun karsa · Tut wuri handayani.', attribution:'Ki Hadjar Dewantara', sourceLabel:'Rujukan Kemendikdasmen', source:'https://itjen.kemendikdasmen.go.id/web/mengenal-lebih-dekat-ki-hadjar-dewantara-bapak-pendidikan-indonesia/' },
  { title:'Asah, asih, asuh', text:'Ilmu, kasih sayang, dan pendampingan menjadi dasar mendidik anak.', attribution:'Sistem Among Ki Hadjar Dewantara', sourceLabel:'Rujukan Kemendikdasmen', source:'https://puslapdik.kemendikdasmen.go.id/shardiknas-2026-dasar-pendidikan-itu-asah-asih-dan-asuh/' },
  { title:'Panca Dharma', text:'Kemerdekaan · kodrat alam · kebudayaan · kebangsaan · kemanusiaan.', attribution:'Lima dasar pendidikan Ki Hadjar Dewantara', sourceLabel:'Rujukan Kemendikdasmen', source:'https://bpmpriau.kemendikdasmen.go.id/2017/05/02/mengenal-sosok-ki-hajar-dewantara-bapak-pendidikan-nasional/' },
  { title:'Pendidikan menuntun', text:'Tuntun bakat, minat, dan potensi peserta didik menuju keselamatan dan kebahagiaan.', attribution:'Cita-cita pendidikan Ki Hadjar Dewantara', sourceLabel:'Rujukan Kemendikbud', source:'https://kebudayaan.kemdikbud.go.id/hardiknas-2023/' },
  { title:'Nelson Mandela', text:'“Pendidikan adalah senjata paling ampuh yang dapat digunakan untuk mengubah dunia.”', attribution:'Terjemahan dari bahasa Inggris', sourceLabel:'Sumber kutipan · UNESCO', source:'https://unesdoc.unesco.org/in/rest/annotationSVC/DownloadWatermarkedAttachment/attach_import_d6de347e-9dec-4e2f-8840-4a0cdb9ec45b?_=216383eng.pdf' },
  { title:'Malala Yousafzai', text:'“Satu anak, satu guru, satu buku, dan satu pena dapat mengubah dunia.”', attribution:'Terjemahan dari bahasa Inggris', sourceLabel:'Sumber kutipan · UNESCO', source:'https://www.unesco.org/en/articles/bonita-young-change-maker-inspires-girls-and-women-nepal-through-education' },
  { title:'John Dewey', text:'“Pendidikan adalah proses menjalani kehidupan, bukan persiapan untuk hidup di masa depan.”', attribution:'Terjemahan dari bahasa Inggris', sourceLabel:'Teks asli · My Pedagogic Creed', source:'https://www.marxists.org/archive/dewey/education.htm' }
]
const clubs = [
  { id:'science', name:'Klub Sains', icon:'🔎', color:'#eaf7ef', category:'Sains & eksperimen', duration:8, description:'Mengamati hal-hal menarik di sekitar dan mencari tahu alasannya.', activities:[
    {id:'shadow',title:'Misi bayangan',kind:'quiz',steps:['Pilih benda di tempat terang dan aman.','Amati bayangan benda selama beberapa saat.','Pikirkan apa yang membuat bayangan terlihat.'],prompt:'Apa yang diperlukan agar bayangan benda terlihat?',options:['Cahaya dan benda yang menghalangi cahaya','Suara yang keras','Air yang mengalir'],correct:0,explanation:'Bayangan terbentuk ketika benda menghalangi cahaya.',safety:'Jangan menatap matahari. Amati bayangan benda saja.'},
    {id:'plants',title:'Detektif tanaman',kind:'write',steps:['Pilih tanaman yang bisa diamati tanpa memetiknya.','Lihat warna daun, bentuk batang, dan kondisi tanah.','Catat perubahan atau hal menarik yang kamu temukan.'],prompt:'Tuliskan satu hal yang kamu amati dan satu pertanyaan tentang tanaman itu.',safety:'Jangan memetik atau mencicipi tanaman. Minta izin orang dewasa sebelum mendekati tanaman yang tidak dikenal.'},
    {id:'weather',title:'Jurnal cuaca',kind:'write',steps:['Lihat keadaan langit dari tempat aman.','Pilih kata untuk menggambarkan cuaca hari ini.','Bandingkan dengan cuaca kemarin jika kamu ingat.'],prompt:'Gambarkan cuaca hari ini dan tuliskan satu tanda yang kamu lihat.'}
  ]},
  { id:'coding', name:'Coding & Logika', icon:'🤖', color:'#edf1ff', category:'Teknologi & logika', duration:7, description:'Latihan menyusun langkah, mencari pola, dan memperbaiki urutan.', activities:[
    {id:'sequence',title:'Antarkan robot',kind:'quiz',steps:['Bayangkan robot mulai dari titik awal.','Robot hanya bisa berjalan maju atau berbelok.','Pilih urutan langkah yang sampai ke tujuan.'],prompt:'Robot menghadap ke kanan. Mana urutan paling tepat agar maju satu kotak lalu berbelok ke atas?',options:['Maju, lalu belok kiri','Belok kanan, lalu maju','Maju dua kali'],correct:0,explanation:'Jika robot menghadap ke kanan, belok kiri membuatnya menghadap ke atas.'},
    {id:'pattern',title:'Lanjutkan polanya',kind:'quiz',steps:['Perhatikan urutan bentuk.','Cari bagian yang berulang.','Pilih bentuk yang meneruskan pola.'],prompt:'Pola: lingkaran, segitiga, lingkaran, segitiga, ... Bentuk berikutnya?',options:['Lingkaran','Persegi','Segitiga'],correct:0,explanation:'Pola bergantian antara lingkaran dan segitiga.'},
    {id:'debug',title:'Cari langkah yang keliru',kind:'write',steps:['Baca urutan instruksi sederhana.','Bayangkan hasil dari setiap langkah.','Jelaskan langkah yang perlu diperbaiki.'],prompt:'Robot diminta maju tiga langkah, tetapi urutannya: maju, maju, berhenti. Apa yang perlu ditambahkan?'}
  ]},
  { id:'literacy', name:'Cerita & Jurnalistik', icon:'📝', color:'#fff3df', category:'Literasi & komunikasi', duration:10, description:'Membuat cerita, judul berita, dan catatan dari lingkungan sekitar.', activities:[
    {id:'headline',title:'Pilih judul berita',kind:'quiz',steps:['Bayangkan sekolah mengadakan kebun kelas.','Berita yang baik menjelaskan hal penting dengan jelas.','Pilih judul yang paling sesuai.'],prompt:'Kelas 4 menanam cabai bersama di halaman sekolah. Judul mana yang paling jelas?',options:['Siswa Kelas 4 Membuat Kebun Cabai','Hari yang Sangat Panjang','Tanaman dan Banyak Hal'],correct:0,explanation:'Judul yang baik menyebutkan inti peristiwa secara jelas.'},
    {id:'story',title:'Cerita tiga kalimat',kind:'write',steps:['Pilih tokoh: anak, hewan, atau benda.','Tentukan tempat dan masalah kecil yang dihadapi.','Akhiri cerita dengan solusi yang baik.'],prompt:'Tulis cerita pendek tiga kalimat. Tidak perlu menyebutkan nama lengkap atau alamat.'},
    {id:'reporter',title:'Reporter cilik',kind:'write',steps:['Pilih kegiatan umum di rumah atau sekolah.','Susun pertanyaan: apa, siapa, kapan, dan bagaimana.','Tuliskan laporan singkat tanpa data pribadi.'],prompt:'Tuliskan tiga hal menarik dari sebuah kegiatan yang kamu lihat.'}
  ]},
  { id:'arts', name:'Seni & Budaya Nusantara', icon:'🎨', color:'#f8eafa', category:'Karya & budaya', duration:10, description:'Mencoba ide karya dan mengenal ragam seni dari berbagai daerah.', activities:[
    {id:'rhythm',title:'Tebak pola bunyi',kind:'quiz',steps:['Tepuk tangan pelan mengikuti pola.','Dengarkan bagian yang berulang.','Pilih bunyi yang melengkapi pola.'],prompt:'Pola tepuk: pelan, cepat, pelan, cepat, ... Selanjutnya?',options:['Pelan','Cepat-cepat','Diam selamanya'],correct:0,explanation:'Pola bergantian pelan lalu cepat, jadi berikutnya pelan.'},
    {id:'motif',title:'Rancang motif daerahmu',kind:'write',steps:['Amati pola pada kain, anyaman, ukiran, atau benda di sekitarmu.','Pilih dua bentuk dan dua warna.','Buat motif sendiri di kertas, tanpa menyalin karya orang lain.'],prompt:'Ceritakan bentuk dan warna yang kamu pilih untuk motifmu.'},
    {id:'soundscape',title:'Bunyi di sekitarku',kind:'write',steps:['Dengarkan bunyi dari tempat yang aman.','Pilih bunyi alam atau kegiatan sehari-hari.','Gambarkan bunyi itu dengan kata atau irama tepukan.'],prompt:'Tuliskan dua bunyi yang kamu dengar dan suasana yang ditimbulkannya.'}
  ]}
]
const currentPhilosophy = computed(() => philosophies[philosophyIndex.value])
let philosophyTimer
const greeting = computed(() => { const h = new Date().getHours(); return h < 11 ? 'Selamat pagi' : h < 15 ? 'Selamat siang' : h < 18 ? 'Selamat sore' : 'Selamat malam' })
const currentQuestion = computed(() => questions.value[questionIndex.value])
const selectableVoices = computed(() => {
  const indonesian = speechVoices.value.filter(voice => /^id(?:-|$)/i.test(voice.lang || ''))
  return indonesian.length ? indonesian : speechVoices.value
})
const lessonContentSections = computed(() => {
  const content = activeLesson.value?.content || ''
  const headings = ['TUJUAN BELAJAR', 'MATERI INTI', 'CONTOH TERBIMBING', 'LATIHAN MANDIRI', 'RANGKUMAN']
  const escaped = headings.join('|')
  return content.split(new RegExp(`\\n\\n(?=${escaped}\\n)`, 'g')).map((block) => {
    const lineBreak = block.indexOf('\n')
    const heading = lineBreak < 0 ? '' : block.slice(0, lineBreak)
    return { heading, text: lineBreak < 0 ? block : block.slice(lineBreak + 1), objective: heading === 'TUJUAN BELAJAR' }
  }).filter(section => section.heading || section.text)
})
const answeredCount = computed(() => answers.value.filter(Boolean).length)
const progressPercent = computed(() => Math.min(100, Math.round((history.value.length / 8) * 100)))
const extracurricularCompleted = computed(() => Object.keys(extracurricularState.value.completed || {}).length)
const extracurricularTotal = computed(() => clubs.reduce((count, club) => count + club.activities.length, 0))
const earnedClubBadges = computed(() => clubs.filter(club => club.activities.every(activity => extracurricularState.value.completed[`${club.id}:${activity.id}`])))
const selectedActivityComplete = computed(() => selectedClub.value && selectedActivity.value ? Boolean(extracurricularState.value.completed[`${selectedClub.value.id}:${selectedActivity.value.id}`]) : false)
function clubCompletedCount(club) { return club.activities.filter(activity => extracurricularState.value.completed[`${club.id}:${activity.id}`]).length }
const weeklyHistory = computed(() => history.value.filter(item => Date.now() - new Date(item.completedAt).getTime() < 7 * 86400000))
const localExample = computed(() => {
  const subject = (selectedSubject.value?.name || '').toLowerCase()
  const examples = subject.includes('matematika') || subject.includes('math') ? 'Coba hubungkan dengan menghitung uang jajan, buah di pasar, atau jarak saat berangkat sekolah.'
    : subject.includes('bahasa indonesia') ? 'Amati cerita, papan petunjuk, dan percakapan yang kamu temui di rumah atau sekolah.'
    : subject.includes('inggris') ? 'Coba gunakan kosakata ini untuk menyebut benda di rumah dan kegiatan sehari-hari.'
    : subject.includes('ipas') ? 'Hubungkan materi dengan lingkungan sekitar, cuaca, tanaman, hewan, dan kebiasaan warga.'
    : subject.includes('pancasila') || subject.includes('ppkn') ? 'Pikirkan contoh sikap saling membantu dan bermusyawarah di rumah atau lingkunganmu.'
    : subject.includes('pai') || subject.includes('agama') ? 'Coba temukan contoh nilai kebaikan ini dalam kegiatan sehari-hari bersama keluarga.'
    : 'Cari contoh dari permainan, karya, makanan, atau kebiasaan yang ada di daerahmu.'
  return examples
})
const studyHint = computed(() => {
  const subject = (selectedSubject.value?.name || '').toLowerCase()
  if (subject.includes('matematika') || subject.includes('math') || subject.includes('hitung')) return 'Baca angka dan satuannya pelan-pelan. Tanyakan pada diri sendiri: operasi apa yang diminta?'
  if (subject.includes('bahasa')) return 'Perhatikan kata kunci dalam pertanyaan. Baca lagi kalimat atau pilihan yang paling sesuai dengan konteks.'
  if (subject.includes('ipas')) return 'Ingat kembali ciri, fungsi, atau urutan proses yang dipelajari. Petunjuknya biasanya ada pada kata penting di soal.'
  return 'Ingat kembali materi yang baru dibaca. Cari kata kunci pada soal, lalu cocokkan dengan contoh yang paling dekat.'
})
function subjectLogo(name) {
  const logos = {
    'matematika': 'subject-matematika.svg', 'bahasa indonesia': 'subject-bahasa-indonesia.svg',
    'bahasa inggris': 'subject-bahasa-inggris.svg', 'hitung cepat': 'subject-hitung-cepat.svg',
    'ipas': 'subject-ipas.svg', 'bahasa arab': 'subject-bahasa-arab.svg', 'pai': 'subject-pai.svg',
    'mathematics in english': 'subject-math-english.svg', 'fiqih': 'subject-fiqih.svg',
    'akidah akhlak': 'subject-akidah-akhlak.svg', 'sejarah kebudayaan islam': 'subject-ski.svg',
    'sejarah indonesia': 'subject-sejarah-indonesia.svg'
  }
  return `/` + (logos[(name || '').toLowerCase()] || 'subject-matematika.svg')
}
async function request(path, options={}) {
  const headers = { 'Content-Type':'application/json', ...(options.headers || {}) }
  if (authToken.value) headers.Authorization = `Bearer ${authToken.value}`
  const curriculumCacheable = /^\/(subjects(?:\/\d+\/lessons)?(?:\?grade=[1-6])?|lessons\/\d+(?:\/questions)?)$/.test(path)
  const progressCacheable = /^\/progress(?:\?.*)?$/.test(path) && account.value?.role === 'STUDENT'
  const cacheable = (!options.method || options.method === 'GET') && (curriculumCacheable || progressCacheable)
  const cacheKey = `tb-curriculum:${account.value?.id || 'public'}:grade-${selectedGrade.value}:${path}`
  let response
  try { response = await fetch(`${API}${path}`, { ...options, headers }) }
  catch (networkError) {
    if (cacheable) { const cached = await readStudyCache(cacheKey); if (cached) return cached }
    throw networkError
  }
  if (!response.ok) { let message = `Permintaan gagal (${response.status})`; try { const body=await response.json(); message=body.message || body.detail || message } catch {} throw new Error(message) }
  if (response.status === 204) return null
  const data = await response.json()
  if (cacheable) writeStudyCache(cacheKey, data)
  if (options.withHeaders) return { data, headers: response.headers }
  return data
}
let studyDbPromise
function studyDb() {
  if (!('indexedDB' in window)) return Promise.resolve(null)
  if (!studyDbPromise) studyDbPromise = new Promise(resolve => {
    const open = indexedDB.open('teman-belajar-offline', 1)
    open.onupgradeneeded = () => open.result.createObjectStore('curriculum')
    open.onsuccess = () => resolve(open.result)
    open.onerror = () => resolve(null)
  })
  return studyDbPromise
}
async function readStudyCache(key) {
  const db = await studyDb(); if (!db) return null
  return new Promise(resolve => { const tx=db.transaction('curriculum','readonly'); const get=tx.objectStore('curriculum').get(key); get.onsuccess=()=>resolve(get.result ?? null); get.onerror=()=>resolve(null) })
}
async function writeStudyCache(key, value) {
  const db = await studyDb(); if (!db) return
  try { const tx=db.transaction('curriculum','readwrite'); tx.objectStore('curriculum').put(value,key) } catch {}
}
function updateOfflineStatus() { offlineMode.value = !navigator.onLine; if (navigator.onLine) syncExtracurricularState() }
function adjustTextScale(delta) { textScale.value = Math.max(1, Math.min(1.3, Math.round((textScale.value + delta) * 100) / 100)); localStorage.setItem('tb_text_scale', String(textScale.value)) }
function speak(text) {
  if (!('speechSynthesis' in window)) { error.value = 'Audio baca belum didukung oleh browser ini.'; return }
  window.speechSynthesis.cancel()
  const utterance = new SpeechSynthesisUtterance(text)
  utterance.lang = 'id-ID'; utterance.rate = 0.88
  utterance.voice = speechVoices.value.find(voice => voice.voiceURI === selectedVoiceURI.value) || null
  utterance.onstart = () => { speechPlaying.value = true }
  utterance.onend = utterance.onerror = () => { speechPlaying.value = false }
  window.speechSynthesis.speak(utterance)
}
function speakQuestion() { if (speechPlaying.value) { window.speechSynthesis.cancel(); speechPlaying.value = false } else speak(currentQuestion.value?.prompt || '') }
function loadSpeechVoices() {
  if (!('speechSynthesis' in window)) return
  speechVoices.value = window.speechSynthesis.getVoices()
}
function chooseSpeechVoice() {
  localStorage.setItem('tb_speech_voice', selectedVoiceURI.value)
}
function restoreExtracurricularState() {
  try {
    extracurricularState.value = JSON.parse(localStorage.getItem(`tb_extracurricular_${account.value?.id}`) || 'null') || { interests: [], completed: {} }
  } catch { extracurricularState.value = { interests: [], completed: {} } }
}
function extracurricularFingerprint(state) {
  return JSON.stringify({ interests: [...(state.interests || [])].sort(), completed: Object.keys(state.completed || {}).sort() })
}
function saveExtracurricularState() {
  if (!account.value?.id) return
  const userId = account.value.id
  const state = JSON.parse(JSON.stringify(extracurricularState.value))
  localStorage.setItem(`tb_extracurricular_${userId}`, JSON.stringify(state))
  request('/extracurricular/progress', { method:'PUT', body:JSON.stringify(state) }).then(remote => {
    localStorage.setItem(`tb_extracurricular_${userId}`, JSON.stringify(remote))
    localStorage.setItem(`tb_extracurricular_synced_${userId}`, extracurricularFingerprint(remote))
    extracurricularState.value = remote
  }).catch(() => {})
}
async function syncExtracurricularState() {
  if (!account.value?.id || !authToken.value) return
  const userId = account.value.id
  try {
    const remote = await request('/extracurricular/progress')
    const local = extracurricularState.value
    const lastSynced = localStorage.getItem(`tb_extracurricular_synced_${userId}`)
    const hasLocalData = (local.interests?.length || 0) + Object.keys(local.completed || {}).length > 0
    const localHasChanges = lastSynced ? extracurricularFingerprint(local) !== lastSynced : hasLocalData
    if (localHasChanges) {
      extracurricularState.value = { interests: [...(local.interests || [])], completed: { ...(remote.completed || {}), ...(local.completed || {}) } }
      saveExtracurricularState()
    } else {
      extracurricularState.value = remote
      localStorage.setItem(`tb_extracurricular_${userId}`, JSON.stringify(remote))
      localStorage.setItem(`tb_extracurricular_synced_${userId}`, extracurricularFingerprint(remote))
    }
  } catch {}
}
function openExtracurricular() {
  selectedNav.value = 'extracurricular'; extracurricularView.value = true; selectedClub.value = null; selectedActivity.value = null
  monitoringView.value = false; curriculumView.value = false; donationAdminView.value = false; selectedSubject.value = null; activeLesson.value = null; finished.value = false
}
function openClub(club) { selectedClub.value = club; selectedActivity.value = null; selectedClubAnswer.value = null; clubAnswerFeedback.value = ''; clubDraft.value = '' }
function openClubActivity(activity) { selectedActivity.value = activity; selectedClubAnswer.value = null; clubAnswerFeedback.value = ''; clubDraft.value = '' }
function toggleClubInterest(club) {
  const interests = new Set(extracurricularState.value.interests || [])
  interests.has(club.id) ? interests.delete(club.id) : interests.add(club.id)
  extracurricularState.value = { ...extracurricularState.value, interests: [...interests] }; saveExtracurricularState()
}
function completeClubActivity(message = '') {
  if (!selectedClub.value || !selectedActivity.value) return
  const key = `${selectedClub.value.id}:${selectedActivity.value.id}`
  extracurricularState.value = { ...extracurricularState.value, completed: { ...extracurricularState.value.completed, [key]: new Date().toISOString() } }
  saveExtracurricularState(); clubAnswerFeedback.value = message || 'Hebat, kegiatan ini selesai! Karya atau catatanmu tetap milikmu di perangkat ini.'
}
function submitClubAnswer() {
  if (selectedClubAnswer.value === null || !selectedActivity.value) return
  if (selectedClubAnswer.value === selectedActivity.value.correct) {
    completeClubActivity(`Benar! ${selectedActivity.value.explanation} Kegiatan ini selesai.`)
  } else clubAnswerFeedback.value = 'Belum tepat. Coba baca petunjuknya sekali lagi dan pilih jawaban lain.'
}
function copyWeeklySummary() {
  const avg = weeklyHistory.value.length ? Math.round(weeklyHistory.value.reduce((sum, item) => sum + item.percent, 0) / weeklyHistory.value.length) : 0
  const message = `Ringkasan belajar ${studentName.value} minggu ini: ${weeklyHistory.value.length} latihan selesai, rata-rata ${avg}%. Terus semangat belajar!`
  if (!navigator.clipboard?.writeText) { summaryMessage.value = message; return }
  navigator.clipboard.writeText(message).then(() => { summaryMessage.value = 'Ringkasan berhasil disalin untuk dibagikan.' }).catch(() => { summaryMessage.value = message })
}
function selectPractice(lesson, count = 10) { openLesson(lesson, count) }
async function submitAuth() {
  authError.value = ''
  if (authBusy.value) return
  if (authView.value === 'register' && registerForm.value.password !== registerForm.value.confirmPassword) { authError.value = 'Konfirmasi kata sandi belum sama.'; return }
  authBusy.value = true
  try {
    const isRegister = authView.value === 'register'
    const payload = isRegister
      ? { name: registerForm.value.name, username: registerForm.value.username.trim(), email: registerForm.value.email, grade: registerForm.value.grade, password: registerForm.value.password }
      : loginForm.value
    const session = await request(`/auth/${isRegister ? 'register' : 'login'}`, { method: 'POST', body: JSON.stringify(payload) })
    authToken.value = session.token
    localStorage.setItem('tb_auth_token', session.token)
    account.value = session.user
    localStorage.setItem('tb_cached_account', JSON.stringify(session.user))
    restoreExtracurricularState(); syncExtracurricularState()
    studentName.value = session.user.name
    selectedGrade.value = viewGradeFor(session.user)
    gradeForm.value = String(session.user.grade)
    localStorage.setItem('tb_student_name', studentName.value)
    localStorage.setItem('tb_selected_grade', selectedGrade.value)
    authReady.value = true
    await loadAll()
  } catch (e) { authError.value = e.message }
  finally { authBusy.value = false }
}
function viewGradeFor(user) {
  const saved = user.role === 'ADMIN' ? localStorage.getItem('tb_admin_view_grade') : null
  return /^[1-6]$/.test(saved || '') ? saved : user.grade
}
async function bootstrapAuth() {
  if (!authToken.value) { authReady.value = true; return }
  try {
    account.value = await request('/auth/me')
    localStorage.setItem('tb_cached_account', JSON.stringify(account.value))
    restoreExtracurricularState(); syncExtracurricularState()
    studentName.value = account.value.name
    selectedGrade.value = viewGradeFor(account.value)
    gradeForm.value = String(account.value.grade)
    authReady.value = true
    await loadAll()
  } catch {
    let cachedAccount = null
    try { cachedAccount = JSON.parse(localStorage.getItem('tb_cached_account') || 'null') } catch {}
    if (cachedAccount?.role === 'STUDENT') {
      account.value = cachedAccount; studentName.value = cachedAccount.name; selectedGrade.value = cachedAccount.grade; gradeForm.value = String(cachedAccount.grade)
      restoreExtracurricularState()
      authReady.value = true; await loadAll()
    } else {
      authToken.value = ''; localStorage.removeItem('tb_auth_token'); authReady.value = true
    }
  }
}
async function logout() {
  try { await request('/auth/logout', { method: 'POST' }) } catch {}
  authToken.value = ''; account.value = null; authReady.value = true
  localStorage.removeItem('tb_auth_token'); localStorage.removeItem('tb_cached_account'); home()
}
async function openMonitoring() {
  selectedNav.value = 'monitoring'
  extracurricularView.value = false; curriculumView.value = false; monitoringView.value = true; donationAdminView.value = false; selectedSubject.value = null; activeLesson.value = null; selectedMonitorStudent.value = null
  adminLoading.value = true; adminError.value = ''
  try { adminStudents.value = await request('/admin/students') }
  catch (e) { adminError.value = e.message }
  finally { adminLoading.value = false }
}
async function openCurriculum() {
  selectedNav.value = 'curriculum'
  extracurricularView.value = false; monitoringView.value = false; curriculumView.value = true; donationAdminView.value = false; selectedSubject.value = null; activeLesson.value = null
  curriculumLoading.value = true; curriculumError.value = ''
  try {
    const data = await request('/admin/curriculum')
    curriculumRows.value = data.catalog || []
    curriculumSourceChecks.value = (data.catalog || []).flatMap(row => row.sources || [])
  } catch (e) { curriculumError.value = e.message }
  finally { curriculumLoading.value = false }
}
async function openDonationAdmin() {
  selectedNav.value = 'donations'
  extracurricularView.value = false; monitoringView.value = false; curriculumView.value = false; donationAdminView.value = true
  selectedSubject.value = null; activeLesson.value = null; donationAdminError.value = ''; donationMessage.value = ''
  donationAdminLoading.value = true
  try { donationStatus.value = await request('/donations/admin') }
  catch (e) { donationAdminError.value = e.message }
  finally { donationAdminLoading.value = false }
}
async function uploadDonationQr() {
  donationAdminError.value = ''; donationMessage.value = ''
  if (!donationFile.value) { donationAdminError.value = 'Pilih gambar QRIS terlebih dahulu.'; return }
  donationUploading.value = true
  try {
    const body = new FormData(); body.append('image', donationFile.value); body.append('note', donationNote.value)
    const response = await fetch(`${API}/donations/admin`, { method:'POST', headers:{ Authorization:`Bearer ${authToken.value}` }, body })
    const result = await response.json()
    if (!response.ok) throw new Error(result.message || `Permintaan gagal (${response.status})`)
    donationMessage.value = result.message
    donationFile.value = null; donationNote.value = ''
    await refreshDonationAdmin()
  } catch (e) { donationAdminError.value = e.message }
  finally { donationUploading.value = false }
}
async function refreshDonationAdmin() { try { donationStatus.value = await request('/donations/admin') } catch (e) { donationAdminError.value = e.message } }
function openDonation() { donationOpen.value = true }
async function checkCurriculumSources() {
  if (curriculumChecking.value) return
  curriculumChecking.value = true; curriculumError.value = ''
  try {
    curriculumSourceChecks.value = await request('/admin/curriculum/check', { method: 'POST' })
    const statusByKey = new Map(curriculumSourceChecks.value.map(source => [source.key, source]))
    curriculumRows.value = curriculumRows.value.map(row => ({ ...row, sources: row.sources.map(source => statusByKey.get(source.key) || source) }))
  } catch (e) { curriculumError.value = e.message }
  finally { curriculumChecking.value = false }
}
async function acknowledgeCurriculumSource(source) {
  try {
    const updated = await request(`/admin/curriculum/${encodeURIComponent(source.key)}/acknowledge`, { method: 'POST' })
    curriculumSourceChecks.value = curriculumSourceChecks.value.map(item => item.key === source.key ? updated : item)
    curriculumRows.value = curriculumRows.value.map(row => ({ ...row, sources: row.sources.map(item => item.key === source.key ? updated : item) }))
  } catch (e) { curriculumError.value = e.message }
}
function requestDeleteStudent(student) { adminError.value = ''; studentToDelete.value = student }
async function deleteStudent() {
  const student = studentToDelete.value
  if (!student || deletingStudentId.value) return
  deletingStudentId.value = student.id
  try {
    await request(`/admin/students/${student.id}`, { method: 'DELETE' })
    adminStudents.value = adminStudents.value.filter(item => item.id !== student.id)
    if (selectedMonitorStudent.value?.id === student.id) selectedMonitorStudent.value = null
    studentToDelete.value = null
  } catch (e) { adminError.value = e.message }
  finally { deletingStudentId.value = null }
}
async function loadAll() {
  loading.value = true; error.value = ''
  try { subjects.value = await request(`/subjects${adminGradeQuery()}`); await loadProgress() }
  catch (e) {
    if (/\b401\b/.test(e.message)) {
      authToken.value = ''; account.value = null
      localStorage.removeItem('tb_auth_token'); localStorage.removeItem('tb_cached_account')
      authError.value = 'Sesi login sudah berakhir. Silakan masuk kembali.'
    } else error.value = `Belum bisa terhubung ke backend (${e.message}). Pastikan backend berjalan dan dapat dijangkau frontend, lalu tekan Coba lagi.`
  }
  finally { loading.value = false }
}
async function loadProgress() { try { const p = await request(`/progress?studentName=${encodeURIComponent(studentName.value)}`); history.value = p.history || [] } catch { history.value = [] } }
function adminGradeQuery() { return account.value?.role === 'ADMIN' ? `?grade=${selectedGrade.value}` : '' }
async function changeAdminGrade(grade) {
  selectedGrade.value = grade
  localStorage.setItem('tb_admin_view_grade', grade)
  selectedSubject.value = null; activeLesson.value = null; lessons.value = []; allSubjectLessons.value = []
  await loadAll()
}
async function saveGrade() {
  if (!account.value || account.value.role === 'ADMIN' || gradeSaving.value || String(gradeForm.value) === String(account.value.grade)) return
  gradeSaving.value = true; gradeMessage.value = ''; gradeError.value = ''
  try {
    const updated = await request('/auth/me/grade', { method:'PUT', body:JSON.stringify({ grade:String(gradeForm.value) }) })
    account.value = { ...account.value, ...updated }
    selectedGrade.value = updated.grade
    localStorage.setItem('tb_selected_grade', updated.grade)
    selectedSubject.value = null; activeLesson.value = null; lessons.value = []; allSubjectLessons.value = []
    await loadAll()
    gradeMessage.value = `Kelas belajar berhasil diubah ke kelas ${updated.grade}.`
  } catch (e) { gradeError.value = e.message; gradeForm.value = String(account.value.grade) }
  finally { gradeSaving.value = false }
}
async function openSubject(subject) { selectedNav.value = 'subjects'; extracurricularView.value = false; selectedSubject.value = subject; activeLesson.value = null; lessons.value = []; try { allSubjectLessons.value = await request(`/subjects/${subject.id}/lessons${adminGradeQuery()}`); lessons.value = allSubjectLessons.value } catch(e) { error.value = e.message } }
async function openLesson(lesson, questionLimit = 10) {
  activeLesson.value = lesson; questions.value = []; answers.value = []; questionIndex.value = 0; selectedOption.value = null; essayWorking.value = ''; essayAnswer.value = ''; feedback.value = null; score.value = 0; finished.value = false; reviewMode.value = false
  hintVisible.value = false; speechPlaying.value = false; window.speechSynthesis?.cancel()
  questionsNote.value = ''
  try {
    const [detail, qs] = await Promise.all([request(`/lessons/${lesson.id}`), request(`/lessons/${lesson.id}/questions?limit=${questionLimit}`, { cache:'no-store', withHeaders:true })])
    activeLesson.value = detail; questions.value = qs.data; answers.value = Array(questions.value.length).fill(null)
    const exhausted = qs.headers.get('X-Questions-Exhausted')
    questionsNote.value = exhausted === 'today' ? 'Semua soal di bab ini sudah kamu kerjakan hari ini. Soal tidak diulang pada hari yang sama, jadi coba bab lain atau kembali besok, ya!'
      : exhausted === 'partial' ? `Sisa soal baru di bab ini untuk hari ini tinggal ${questions.value.length}. Soal yang sudah kamu kerjakan hari ini tidak diulang.` : ''
  }
  catch(e) { error.value = e.message }
}
async function previewPrint(lesson) {
  printPreview.value = { lesson, questions: [] }; printLoading.value = true; printError.value = ''
  try { printPreview.value = await request(`/lessons/${lesson.id}/print-questions`, { cache:'no-store' }) }
  catch (e) { printError.value = e.message }
  finally { printLoading.value = false }
}
function closePrintPreview() { printPreview.value = null; printError.value = '' }
function printQuestions() { window.print() }
async function checkAnswer() {
  if (!canCheck.value || isAnswered.value) return
  try {
    const result = await request(`/lessons/${activeLesson.value.id}/check`, { method:'POST', body:JSON.stringify({questionId:currentQuestion.value.id, selectedOptionText:isEssay.value ? essayAnswer.value.trim() : currentQuestion.value.options[selectedOption.value]}) })
    answers.value[questionIndex.value] = { selectedOptionIndex:selectedOption.value, essayWorking:essayWorking.value, essayAnswer:essayAnswer.value, feedback:result }
    score.value = answers.value.filter(answer => answer?.feedback.correct).length
    if (questionIndex.value + 1 < questions.value.length) goToQuestion(questionIndex.value + 1)
    else await finishQuiz()
  }
  catch(e) { error.value = e.message }
}
function goToQuestion(index) {
  const firstUnanswered = answers.value.findIndex(answer => !answer)
  const lastUnlocked = firstUnanswered === -1 ? questions.value.length - 1 : firstUnanswered
  if (index < 0 || index > lastUnlocked) return
  questionIndex.value = index
  hintVisible.value = false
  selectedMethod.value = null
  const saved = answers.value[index]
  selectedOption.value = saved?.selectedOptionIndex ?? null
  essayWorking.value = saved?.essayWorking ?? ''
  essayAnswer.value = saved?.essayAnswer ?? ''
  // Explanations stay hidden until every question is answered and the student opens the review.
  feedback.value = reviewMode.value ? saved?.feedback ?? null : null
}
async function finishQuiz() {
  if (answers.value.some(answer => !answer)) return
  finished.value = true; reviewMode.value = false
  if (account.value?.role === 'ADMIN') return
  saving.value = true
  try { await request('/progress', { method:'POST', body:JSON.stringify({studentName:studentName.value.trim() || 'Adik', lessonId:activeLesson.value.id, score:score.value, total:questions.value.length}) }); await loadProgress() }
  catch(e) { error.value = `Nilai tampil, tetapi belum tersimpan: ${e.message}` }
  finally { saving.value = false }
}
function goToFirstUnanswered() { goToQuestion(answers.value.findIndex(answer => !answer)) }
function reviewAnswers() { finished.value = false; reviewMode.value = true; goToQuestion(0) }
function returnToResults() { finished.value = true; reviewMode.value = false }
function setName() { profileOpen.value = !profileOpen.value }
function chooseAvatar(avatar) { studentAvatar.value = avatar; localStorage.setItem('tb_student_avatar', avatar) }
function editStudentName() { const name = window.prompt('Siapa nama panggilanmu?', studentName.value); if (name && name.trim()) { studentName.value = name.trim().slice(0,80); localStorage.setItem('tb_student_name', studentName.value); loadProgress() } }
function home() { selectedNav.value = 'home'; extracurricularView.value = false; monitoringView.value = false; curriculumView.value = false; donationAdminView.value = false; selectedSubject.value = null; activeLesson.value = null; finished.value = false; error.value = ''; window.scrollTo({ top: 0, behavior: 'smooth' }) }
function scrollToId(id) { selectedNav.value = id; extracurricularView.value = false; monitoringView.value = false; curriculumView.value = false; donationAdminView.value = false; selectedSubject.value = null; activeLesson.value = null; finished.value = false; nextTick(() => document.getElementById(id)?.scrollIntoView({ behavior: 'smooth' })) }
function formatDate(value) { return new Date(value).toLocaleString('id-ID') }
onMounted(() => {
  bootstrapAuth()
  loadSpeechVoices()
  window.speechSynthesis?.addEventListener?.('voiceschanged', loadSpeechVoices)
  window.addEventListener('online', updateOfflineStatus); window.addEventListener('offline', updateOfflineStatus)
  if ('serviceWorker' in navigator) navigator.serviceWorker.register('/sw.js').catch(() => {})
  philosophyTimer = window.setInterval(() => { philosophyIndex.value = (philosophyIndex.value + 1) % philosophies.length }, 30000)
})
onUnmounted(() => { window.clearInterval(philosophyTimer); window.removeEventListener('online', updateOfflineStatus); window.removeEventListener('offline', updateOfflineStatus); window.speechSynthesis?.removeEventListener?.('voiceschanged', loadSpeechVoices); window.speechSynthesis?.cancel() })
</script>

<template>
  <div v-if="!authReady" class="auth-loading"><div class="spinner"></div><p>Memeriksa sesi belajar...</p></div>
  <main v-else-if="!authToken" class="auth-screen">
    <section class="auth-art-panel"><button class="brand auth-brand" aria-label="Teman Belajar"><img class="brand-logo" src="/teman-belajar-logo.png" alt="Teman Belajar — Ruang Belajar Ceria"></button><div class="auth-illustration">📚</div><div class="auth-art-copy"><div class="eyebrow">BELAJAR SESUAI KELASMU</div><h1>Kenangan yang Indah tentang belajar dimulai dari sini!</h1><p>Masuk untuk melanjutkan latihan dan melihat perkembangan belajarmu.</p><div class="auth-decoration">✦　🌱　✧</div></div></section>
    <section class="auth-form-panel">
      <form v-if="authView==='login'" class="auth-card" @submit.prevent="submitAuth">
        <div class="eyebrow">SELAMAT DATANG KEMBALI</div><h2>Masuk ke akunmu</h2><p class="auth-intro">Masukkan email dan kata sandi untuk membuka dashboard.</p>
        <label>Email atau username<input v-model.trim="loginForm.identifier" type="text" autocomplete="username" placeholder="nama@email.com atau username" required></label>
        <label>Kata sandi<input v-model="loginForm.password" type="password" autocomplete="current-password" placeholder="Minimal 8 karakter" required></label>
        <p v-if="authError" class="auth-error" role="alert">{{ authError }}</p>
        <button class="primary-btn auth-submit" type="submit" :disabled="authBusy">{{ authBusy ? 'Memeriksa...' : 'Masuk' }} <span>→</span></button>
        <p class="auth-switch">Belum punya akun? <button type="button" @click="authView='register';authError=''">Daftar sekarang</button></p>
      </form>
      <form v-else class="auth-card" @submit.prevent="submitAuth">
        <div class="eyebrow">BUAT AKUN BELAJAR</div><h2>Daftar akun baru</h2><p class="auth-intro">Isi data siswa untuk mulai belajar sesuai kelas.</p>
        <label>Nama panggilan<input v-model.trim="registerForm.name" type="text" autocomplete="name" maxlength="120" placeholder="Nama kamu" required></label>
        <label>Username<input v-model.trim="registerForm.username" type="text" autocomplete="username" minlength="3" maxlength="50" pattern="[a-zA-Z0-9._-]{3,50}" placeholder="Contoh: adikbelajar" required></label>
        <label>Email<input v-model.trim="registerForm.email" type="email" autocomplete="email" maxlength="190" placeholder="nama@email.com" required></label>
        <label>Kelas<select v-model="registerForm.grade" required><option v-for="grade in 6" :key="grade" :value="String(grade)">Kelas {{ grade }}</option></select></label>
        <label>Kata sandi<input v-model="registerForm.password" type="password" autocomplete="new-password" minlength="8" maxlength="100" placeholder="Minimal 8 karakter" required></label>
        <label>Ulangi kata sandi<input v-model="registerForm.confirmPassword" type="password" autocomplete="new-password" minlength="8" maxlength="100" placeholder="Ketik ulang kata sandi" required></label>
        <p v-if="authError" class="auth-error" role="alert">{{ authError }}</p>
        <button class="primary-btn auth-submit" type="submit" :disabled="authBusy">{{ authBusy ? 'Membuat akun...' : 'Daftar' }} <span>→</span></button>
        <p class="auth-switch">Sudah punya akun? <button type="button" @click="authView='login';authError=''">Masuk</button></p>
      </form>
    </section>
  </main>
  <div v-else class="app-shell" :class="{'text-large':textScale>1}" :style="{'--text-scale':textScale}">
    <aside class="sidebar">
      <button class="brand" aria-label="Kembali ke beranda" @click="home"><img class="brand-logo" src="/teman-belajar-logo.png" alt="Teman Belajar — Ruang Belajar Ceria"></button>
      <div class="side-label">MENU BELAJAR</div>
      <button class="nav-item" title="Beranda" :class="{active:selectedNav==='home'}" @click="home"><span>⌂</span> Beranda</button>
      <button class="nav-item" title="Mata pelajaran" :class="{active:selectedNav==='subjects'}" @click="scrollToId('subjects')"><span>▦</span> Mata pelajaran</button>
      <button class="nav-item" title="Ekskul" :class="{active:selectedNav==='extracurricular'}" @click="openExtracurricular"><span>✦</span> Ekskul</button>
      <button class="nav-item" title="Perkembanganku" :class="{active:selectedNav==='progress'}" @click="scrollToId('progress')"><span>♧</span> Perkembanganku</button>
      <button v-if="account?.role==='ADMIN'" class="nav-item" title="Monitoring siswa" :class="{active:selectedNav==='monitoring'}" @click="openMonitoring"><span>▤</span> Monitoring siswa</button>
      <button v-if="account?.role==='ADMIN'" class="nav-item" title="Cek kurikulum" :class="{active:selectedNav==='curriculum'}" @click="openCurriculum"><span>↻</span> Cek kurikulum</button>
      <button v-if="account?.role==='ADMIN'" class="nav-item" title="QRIS Mandiri" :class="{active:selectedNav==='donations'}" @click="openDonationAdmin"><span>♡</span> QRIS Mandiri</button>
      <div class="sidebar-bottom philosophy-card"><Transition name="philosophy-fade" mode="out-in"><div :key="philosophyIndex" aria-live="polite"><strong>{{ currentPhilosophy.title }}</strong><p>{{ currentPhilosophy.text }}</p><small>{{ currentPhilosophy.attribution }}</small><a class="philosophy-source" :href="currentPhilosophy.source" target="_blank" rel="noopener noreferrer">{{ currentPhilosophy.sourceLabel }} ↗</a></div></Transition></div>
      <div class="sidebar-footer">Dibuat untuk belajar dengan gembira 💛</div>
    </aside>
    <main class="main-area">
      <header class="topbar"><div class="breadcrumb">Ruang belajar <span>/</span> <b>{{ activeLesson ? activeLesson.title : selectedSubject ? selectedSubject.name : extracurricularView ? (selectedActivity?.title || selectedClub?.name || 'Ekskul') : 'Beranda' }}</b></div><div class="topbar-actions"><div v-if="account?.role==='ADMIN'" class="text-controls" aria-label="Ukuran tulisan"><button type="button" aria-label="Kecilkan tulisan" @click="adjustTextScale(-0.1)">A−</button><button type="button" aria-label="Besarkan tulisan" @click="adjustTextScale(0.1)">A+</button></div><button v-if="account?.role!=='ADMIN'" class="topbar-donate-btn" type="button" @click="openDonation"><span>♡</span> Donasi</button><div class="profile-wrap"><button class="profile" aria-label="Pengaturan profil" @click="setName"><span class="avatar">{{ studentAvatar }}</span><span><b>{{ studentName }}</b><small>Kelas {{ selectedGrade }}</small></span></button><section v-if="profileOpen" class="profile-popover"><b>Pilih karakter</b><div class="avatar-options"><button v-for="avatar in studentAvatars" :key="avatar" type="button" :aria-label="`Pilih karakter ${avatar}`" :class="{selected:studentAvatar===avatar}" @click="chooseAvatar(avatar)">{{ avatar }}</button></div><button type="button" class="profile-name-btn" @click="editStudentName">Ubah nama</button></section></div><button class="logout-btn" @click="logout">Keluar</button></div></header>
      <div class="content">
        <div v-if="error" class="error-banner"><span>{{ error }}</span><button @click="loadAll">Coba lagi</button><button class="close-error" @click="error=''">×</button></div>
        <div v-if="offlineMode && authToken" class="offline-banner" role="status">📡 Kamu sedang offline. Materi dan soal yang pernah dibuka bisa dibaca. Pemeriksaan jawaban akan aktif lagi saat tersambung.</div>
        <div v-if="loading" class="loading"><div class="spinner"></div><h3>Menyiapkan ruang belajarmu...</h3></div>
        <template v-else-if="activeLesson">
          <button class="back-link" @click="activeLesson=null; finished=false">← Kembali ke {{ selectedSubject?.name }}</button>
          <section class="lesson-panel">
            <div class="lesson-top"><span class="eyebrow">{{ activeLesson.grade }} · {{ activeLesson.estimatedMinutes }} menit</span><img class="lesson-emoji subject-logo-img" :src="subjectLogo(selectedSubject?.name)" :alt="selectedSubject?.name || 'Mata pelajaran'"></div>
            <h1>{{ activeLesson.title }}</h1><div class="lesson-copy"><section v-for="(section,index) in lessonContentSections" :key="`${section.heading}-${index}`" class="lesson-content-section" :class="{'lesson-objective':section.objective}"><h2 v-if="section.heading">{{ section.heading }}</h2><p>{{ section.text }}</p></section></div>
            <aside class="local-example"><b>🌏 Contoh di sekitar kita</b><p>{{ localExample }}</p></aside>
            <label class="voice-picker">Suara bacaan<select v-model="selectedVoiceURI" @change="chooseSpeechVoice"><option value="">Suara default perangkat</option><option v-for="voice in selectableVoices" :key="voice.voiceURI" :value="voice.voiceURI">{{ voice.name }} · {{ voice.lang }}</option></select><small v-if="!selectableVoices.some(voice => /^id(?:-|$)/i.test(voice.lang || ''))">Suara Bahasa Indonesia belum tersedia di perangkat ini; pilihan suara yang ada tetap bisa dicoba.</small></label>
            <div class="lesson-actions"><button class="secondary-btn lesson-action-btn" @click="speak(activeLesson.title + '. ' + (activeLesson.content || ''))">🔊 Dengarkan materi</button><button class="secondary-btn lesson-action-btn" @click="previewPrint(activeLesson)">Lihat dan cetak soal</button></div>
            <p v-if="questionsNote && questions.length && !finished" class="muted questions-note">{{ questionsNote }}</p>
            <div v-if="!finished && questions.length" class="quiz-box">
              <div v-if="reviewMode" class="review-banner"><span>Mode tinjau: jawaban dan pembahasanmu.</span><button @click="returnToResults">Kembali ke hasil</button></div>
              <div class="quiz-progress"><span>Soal {{ questionIndex+1 }} dari {{ questions.length }} · {{ answeredCount }} dijawab</span><div class="progress-track"><div class="progress-fill" :style="{width: (answeredCount/questions.length*100)+'%'}"></div></div></div>
              <div class="question-review"><b>Nomor soal</b><div class="question-navigation"><button v-for="(_,i) in questions" :key="i" class="question-chip" :class="{current:questionIndex===i, answered:!!answers[i], 'review-right':reviewMode && answers[i]?.feedback.correct, 'review-wrong':reviewMode && answers[i] && !answers[i].feedback.correct}" :disabled="!answers[i] && i!==questionIndex" :aria-label="`Soal ${i+1}${reviewMode && answers[i] ? (answers[i].feedback.correct ? ', benar' : ', salah') : answers[i] ? ', sudah dijawab, tinjau' : ''}`" @click="goToQuestion(i)">{{ i+1 }}</button></div><small>{{ reviewMode ? 'Pilih nomor soal untuk melihat pilihanmu dan pembahasannya.' : 'Pilih nomor yang sudah dijawab untuk melihat kembali pilihanmu. Pembahasan muncul setelah semua soal dijawab.' }}</small></div>
              <div class="question-prompt"><h2>{{ currentQuestion.prompt }}</h2><button type="button" class="audio-btn" :aria-label="speechPlaying ? 'Hentikan audio' : 'Dengarkan soal'" @click="speakQuestion">{{ speechPlaying ? '⏹' : '🔊' }}</button></div>
              <button v-if="!isAnswered && !reviewMode" class="hint-btn" type="button" @click="hintVisible=!hintVisible">{{ hintVisible ? 'Sembunyikan petunjuk' : '💡 Butuh petunjuk?' }}</button><p v-if="hintVisible && !isAnswered" class="hint-card">{{ studyHint }}</p>
              <div v-if="isEssay" class="essay-form"><span class="essay-badge">Soal esai</span><label for="essay-working">Tuliskan langkah-langkahmu</label><textarea id="essay-working" v-model="essayWorking" rows="5" maxlength="1000" :disabled="isAnswered" placeholder="Contoh: pertama aku menghitung ..."></textarea><label for="essay-answer">Jawaban akhir</label><input id="essay-answer" :class="feedback ? (feedback.correct ? 'answer-right' : 'answer-wrong') : ''" v-model="essayAnswer" type="text" maxlength="40" autocomplete="off" :disabled="isAnswered" placeholder="Tulis angkanya"><small>Yang dinilai otomatis hanya jawaban akhir. Langkahmu tetap di perangkat ini dan tidak dikirim ke server.</small></div>
              <div v-else class="options"><button v-for="(option,i) in currentQuestion.options" :key="i" class="option" :class="{'arabic-option':activeLesson.subjectName==='Bahasa Arab', chosen:selectedOption===i, right:feedback && option===feedback.correctOption, wrong:feedback && i===selectedOption && !feedback.correct}" :disabled="isAnswered" @click="selectedOption=i"><span class="option-letter">{{ ['A','B','C','D'][i] || i+1 }}</span><span :dir="activeLesson.subjectName==='Bahasa Arab' ? 'rtl' : 'auto'">{{ option }}</span><span v-if="feedback && (i===selectedOption || option===feedback.correctOption)" class="option-status">{{ option===feedback.correctOption ? '✓' : '✗' }}</span></button></div>
              <div v-if="feedback" class="feedback" :class="feedback.correct?'feedback-good':'feedback-try'"><b>{{ feedback.correct ? 'Hebat! Jawabanmu benar 🎉' : 'Belum tepat, tidak apa-apa! 💪' }}</b><p v-if="isEssay" class="method-title">Jawaban yang benar: {{ feedback.correctOption }}. Bandingkan langkahmu dengan pembahasan ini.</p><template v-if="feedback.longMethod"><div v-if="feedback.quickMethod" class="method-tabs"><button v-for="tab in methodTabs" :key="tab.id" type="button" class="method-tab" :class="{active:activeMethod===tab.id}" :aria-pressed="activeMethod===tab.id" @click="selectedMethod=tab.id">{{ tab.label }}</button></div><p v-else class="method-title">Cara panjang</p><p class="method-steps">{{ activeMethod==='quick' ? feedback.quickMethod : feedback.longMethod }}</p></template><p v-else>{{ feedback.explanation }}</p></div>
              <div v-if="isAnswered && !reviewMode" class="answer-locked"><p>Jawabanmu sudah tersimpan. Pembahasan muncul setelah semua soal dijawab.</p><button type="button" class="secondary-btn" @click="goToFirstUnanswered">Lanjut ke soal berikutnya →</button></div>
              <button v-else-if="!isAnswered" class="primary-btn" :disabled="!canCheck || offlineMode" @click="checkAnswer">{{ offlineMode ? 'Menunggu koneksi untuk memeriksa' : 'Periksa jawaban' }} <span v-if="!offlineMode">→</span></button>
            </div>
            <div v-else-if="finished" class="result-card"><div class="result-star">🏆</div><h2>Latihan selesai!</h2><div class="result-counts"><div><b>{{ score }}</b><span>Benar</span></div><div><b>{{ questions.length-score }}</b><span>Salah</span></div></div><p>{{ score===questions.length ? 'Sempurna! Kamu hebat sekali.' : score/questions.length >= .7 ? 'Kamu sudah memahami banyak hal. Teruskan langkah baikmu!' : 'Tidak apa-apa, belajar perlu waktu. Ayo coba sesi singkat dan pelajari lagi petunjuknya.' }}</p><p v-if="saving" class="muted">Menyimpan hasil...</p><button class="primary-btn" @click="reviewAnswers">Tinjau jawaban dan pembahasan</button><button class="secondary-btn" @click="selectPractice(activeLesson, score/questions.length < .7 ? 5 : 10)">{{ score/questions.length < .7 ? 'Latihan singkat 5 soal' : 'Latihan lagi' }} ↻</button><button class="secondary-btn" @click="home">Pilih pelajaran lain</button></div>
            <div v-else class="empty-state">{{ questionsNote || 'Materi ini belum memiliki latihan. Coba pelajaran lainnya, ya!' }}</div>
          </section>
        </template>
        <template v-else-if="extracurricularView">
          <button class="back-link" @click="selectedActivity ? (selectedActivity=null, clubAnswerFeedback='', clubDraft='') : selectedClub ? (selectedClub=null, selectedActivity=null) : home()">← {{ selectedActivity ? selectedClub?.name : selectedClub ? 'Semua ekskul' : 'Kembali ke beranda' }}</button>
          <template v-if="selectedClub && selectedActivity">
            <section class="club-activity-panel">
              <div class="eyebrow">{{ selectedClub.name }} · KELAS {{ selectedGrade }} · {{ selectedClub.duration }} MENIT</div>
              <h1>{{ selectedActivity.title }}</h1>
              <div class="club-steps"><b>Langkah kegiatan</b><ol><li v-for="step in selectedActivity.steps" :key="step">{{ step }}</li></ol></div>
              <aside v-if="selectedActivity.safety" class="club-safety">🛡️ <b>Ingat keselamatan:</b> {{ selectedActivity.safety }}</aside>
              <section v-if="selectedActivity.kind==='quiz'" class="club-challenge"><h2>{{ selectedActivity.prompt }}</h2><button v-for="(option,index) in selectedActivity.options" :key="option" class="club-option" :class="{chosen:selectedClubAnswer===index,correct:selectedActivityComplete && selectedClubAnswer===selectedActivity.correct}" :disabled="selectedActivityComplete" @click="selectedClubAnswer=index">{{ ['A','B','C'][index] }}. {{ option }}</button><button class="primary-btn" :disabled="selectedClubAnswer===null || selectedActivityComplete" @click="submitClubAnswer">{{ selectedActivityComplete ? 'Kegiatan selesai ✓' : 'Periksa jawaban' }}</button><p v-if="clubAnswerFeedback" class="club-feedback" role="status">{{ clubAnswerFeedback }}</p></section>
              <section v-else class="club-challenge"><h2>{{ selectedActivity.prompt }}</h2><textarea v-model="clubDraft" maxlength="600" rows="5" placeholder="Tulis ide atau hasil pengamatanmu di sini..."></textarea><p class="club-privacy-note">Tulisanmu tetap di perangkat ini dan tidak dikirim ke server.</p><button class="primary-btn" :disabled="clubDraft.trim().length<8 || selectedActivityComplete" @click="completeClubActivity()">{{ selectedActivityComplete ? 'Kegiatan selesai ✓' : 'Tandai kegiatan selesai' }}</button><p v-if="clubAnswerFeedback" class="club-feedback" role="status">{{ clubAnswerFeedback }}</p></section>
              <div v-if="selectedActivityComplete" class="club-completion">🌟 Kegiatan ini sudah masuk ke progresmu.</div>
            </section>
          </template>
          <template v-else-if="selectedClub">
            <section class="section-heading"><div><div class="eyebrow">EKSPLORASI MINAT</div><h1>{{ selectedClub.icon }} {{ selectedClub.name }}</h1><p>{{ selectedClub.description }} · Kelas {{ selectedGrade }}</p></div><span class="progress-pill">{{ clubCompletedCount(selectedClub) }}/{{ selectedClub.activities.length }} kegiatan</span></section>
            <div class="club-activity-list"><article v-for="activity in selectedClub.activities" :key="activity.id" class="club-activity-card"><div class="club-activity-icon">{{ selectedClub.icon }}</div><div class="club-activity-copy"><b>{{ activity.title }}</b><small>{{ selectedClub.duration }} menit · {{ activity.kind==='quiz' ? 'Tantangan interaktif' : 'Karya atau catatan' }}</small></div><span v-if="extracurricularState.completed[`${selectedClub.id}:${activity.id}`]" class="club-done">Selesai ✓</span><button class="club-start-btn" @click="openClubActivity(activity)">Mulai →</button></article></div>
          </template>
          <template v-else>
            <section class="section-heading extracurricular-heading"><div><div class="eyebrow">BELAJAR DI LUAR MATA PELAJARAN</div><h1>Ekskul dan kegiatan seru ✨</h1><p>Pilih kegiatan sesuai minat. Sesi singkat ini bisa dilakukan di rumah atau sekolah.</p></div><span class="progress-pill">{{ extracurricularCompleted }}/{{ extracurricularTotal }} kegiatan selesai</span></section>
            <section class="club-progress-card"><div><b>Perjalanan ekskulmu</b><p>{{ earnedClubBadges.length }} lencana klub diraih. Setiap kegiatan adalah kesempatan mencoba hal baru.</p></div><div class="club-badges"><span v-for="badge in earnedClubBadges" :key="badge.id" :title="badge.name">🏅 {{ badge.name }}</span><span v-if="!earnedClubBadges.length" class="club-no-badges">Lencana pertama menantimu!</span></div></section>
            <div class="club-grid"><article v-for="club in clubs" :key="club.id" class="club-card"><div class="club-card-icon" :style="{background:club.color}">{{ club.icon }}</div><div class="eyebrow">{{ club.category }}</div><h2>{{ club.name }}</h2><p>{{ club.description }}</p><div class="club-card-meta"><span>{{ club.duration }} menit per kegiatan</span><span>{{ clubCompletedCount(club) }}/{{ club.activities.length }} selesai</span></div><div class="club-card-actions"><button class="club-start-btn" @click="openClub(club)">Lihat kegiatan →</button><button class="club-interest-btn" :class="{interested:extracurricularState.interests.includes(club.id)}" @click="toggleClubInterest(club)">{{ extracurricularState.interests.includes(club.id) ? '♥ Minat disimpan' : '♡ Simpan minat' }}</button></div></article></div>
            <p class="club-care-note">Beberapa kegiatan perlu didampingi orang dewasa. Ekskul fisik seperti olahraga dan bela diri tetap perlu pelatih dan ruang latihan yang aman.</p>
          </template>
        </template>
        <template v-else-if="curriculumView && account?.role==='ADMIN'">
          <button class="back-link" @click="home">← Kembali ke dashboard</button>
          <section class="section-heading curriculum-heading"><div><div class="eyebrow">DASHBOARD ADMIN</div><h1>Pemeriksaan kurikulum</h1><p>Bandingkan daftar bab aplikasi dengan buku resmi dan periksa perubahan dokumen sumber.</p></div><button class="primary-btn curriculum-check-btn" :disabled="curriculumChecking || curriculumLoading" @click="checkCurriculumSources">{{ curriculumChecking ? 'Memeriksa sumber...' : 'Periksa sumber resmi ↻' }}</button></section>
          <p class="curriculum-note">Pemeriksaan membandingkan checksum berkas PDF resmi dengan pemeriksaan sebelumnya. Jika berkas berubah, buka sumbernya untuk meninjau apakah ada bab baru atau perubahan materi. Pemeriksaan pertama menyimpan baseline.</p>
          <div v-if="curriculumError" class="error-banner"><span>{{ curriculumError }}</span><button @click="openCurriculum">Coba lagi</button></div>
          <div v-if="curriculumLoading || curriculumChecking" class="loading"><div class="spinner"></div><h3>{{ curriculumChecking ? 'Mengunduh dan membandingkan sumber resmi...' : 'Memuat katalog bab...' }}</h3></div>
          <div v-else-if="curriculumRows.length" class="curriculum-grid">
            <section v-for="row in curriculumRows" :key="`${row.subject}-${row.grade}`" class="curriculum-card">
              <div class="curriculum-card-head"><div><b>{{ row.subject }} · {{ row.grade }}</b><small>{{ row.chapters.length }} bab terdaftar<span v-if="row.supplementalCount"> · {{ row.supplementalCount }} latihan tambahan</span></small></div><span class="progress-pill">Katalog aplikasi</span></div>
              <ol class="curriculum-chapters"><li v-for="chapter in row.chapters" :key="chapter">{{ chapter }}</li></ol>
              <div v-if="row.sources.length" class="curriculum-sources"><div class="curriculum-source-title">Sumber resmi yang dipantau</div><article v-for="source in row.sources" :key="source.key" class="curriculum-source">
                <div class="curriculum-source-info"><b>{{ source.title }}</b><small v-if="source.checkedAt">Pemeriksaan: {{ formatDate(source.checkedAt) }}</small><small v-else>Belum pernah diperiksa</small>
                  <span class="curriculum-status" :class="`status-${(source.status || 'NOT_CHECKED').toLowerCase()}`">{{ source.status==='CHANGED' ? 'Berkas berubah · perlu tinjauan' : source.status==='UNCHANGED' ? 'Tidak ada perubahan berkas' : source.status==='BASELINE_SAVED' ? 'Baseline tersimpan' : source.status==='UNAVAILABLE' ? 'Sumber tidak dapat diakses' : 'Belum diperiksa' }}</span>
                  <small v-if="source.error" class="curriculum-source-error">{{ source.error }}</small>
                </div>
                <div class="curriculum-source-actions"><a :href="source.url" target="_blank" rel="noopener noreferrer">Buka PDF ↗</a><button v-if="source.pendingReview" @click="acknowledgeCurriculumSource(source)">Tandai sudah ditinjau</button></div>
              </article></div>
              <div v-else class="curriculum-no-source">Belum ada PDF buku resmi yang dikonfigurasi untuk pemantauan kelas ini. <a href="https://buku.kemendikdasmen.go.id/katalog/" target="_blank" rel="noopener noreferrer">Buka katalog resmi ↗</a></div>
            </section>
          </div>
          <div v-else class="empty-state">Katalog bab belum tersedia.</div>
        </template>
        <template v-else-if="donationAdminView && account?.role==='ADMIN'">
          <button class="back-link" @click="home">← Kembali ke dashboard</button>
          <section class="section-heading"><div><div class="eyebrow">DASHBOARD ADMIN</div><h1>QRIS Mandiri</h1><p>Kelola QRIS donasi untuk dashboard pengguna.</p></div></section>
          <div v-if="donationAdminError" class="error-banner"><span>{{ donationAdminError }}</span><button @click="openDonationAdmin">Coba lagi</button></div>
          <div v-if="donationMessage" class="donation-notice">{{ donationMessage }}</div>
          <div v-if="donationAdminLoading" class="loading"><div class="spinner"></div><h3>Memuat pengaturan donasi...</h3></div>
          <div v-else class="donation-admin-layout">
            <section class="donation-admin-card"><div class="eyebrow">DAFTARKAN MERCHANT</div><h2>Dapatkan QRIS resmi dari Mandiri</h2><p>Nomor rekening saja belum bisa membentuk QRIS. Daftarkan rekening sebagai rekening merchant melalui Livin’ Usaha. Setelah data usaha diverifikasi, Mandiri menerbitkan QRIS resmi yang dapat diunggah ke aplikasi ini.</p><a class="mandiri-register-link" href="https://qris.bankmandiri.co.id/" target="_blank" rel="noopener noreferrer">Buka Livin’ Usaha Mandiri ↗</a></section>
            <section class="donation-admin-card"><div class="eyebrow">UNGGAH QRIS MANDIRI</div><h2>Minta persetujuan</h2><p>Unggah file QRIS yang diterbitkan Mandiri (PNG, JPG, atau WebP, maksimal 5 MB). Tautan persetujuan sekali pakai akan dikirim ke {{ donationStatus.pendingEmail }}.</p>
              <label class="donation-upload-label">Gambar QRIS<input type="file" accept="image/png,image/jpeg,image/webp" @change="donationFile=$event.target.files?.[0] || null"></label>
              <label class="donation-upload-label">Catatan (opsional)<input v-model="donationNote" type="text" maxlength="500" placeholder="Contoh: QRIS donasi Teman Belajar"></label>
              <button class="primary-btn" :disabled="donationUploading || !donationFile" @click="uploadDonationQr">{{ donationUploading ? 'Mengunggah...' : 'Kirim untuk persetujuan →' }}</button>
            </section>
            <section class="donation-admin-card donation-status-card"><div class="eyebrow">STATUS QR</div><h2>{{ donationStatus.approved ? 'QR aktif' : 'Belum ada QR aktif' }}</h2><p v-if="donationStatus.approved">Disetujui {{ formatDate(donationStatus.approvedAt) }}. QR ini sedang ditampilkan pada dashboard pengguna.</p><p v-else>QR donasi belum terlihat oleh pengguna.</p><img v-if="donationStatus.approved" :src="`${API_ROOT}/api/donations/qr`" alt="QRIS donasi aktif" class="donation-qr-image"><div v-if="donationStatus.pending" class="donation-pending">⏳ Unggahan menunggu persetujuan pemilik website ({{ formatDate(donationStatus.pendingAt) }}).</div></section>
          </div>
        </template>
        <template v-else-if="monitoringView && account?.role==='ADMIN'">
          <button class="back-link" @click="home">← Kembali ke dashboard</button>
          <section class="section-heading"><div><div class="eyebrow">DASHBOARD ADMIN</div><h1>Monitoring siswa</h1><p>Pantau kelas, nilai, dan riwayat latihan semua akun siswa.</p></div></section>
          <div v-if="adminError" class="error-banner"><span>{{ adminError }}</span><button @click="openMonitoring">Coba lagi</button></div>
          <div v-if="adminLoading" class="loading"><div class="spinner"></div><h3>Memuat perkembangan siswa...</h3></div>
          <div v-else class="monitoring-layout">
            <section class="monitor-table-card"><div class="monitor-card-heading"><div><b>Semua siswa</b><small>{{ adminStudents.length }} akun terdaftar</small></div><span class="progress-pill">{{ adminStudents.reduce((sum,s)=>sum+s.completedQuizzes,0) }} latihan</span></div>
              <div v-if="adminStudents.length" class="monitor-table-scroll"><table class="monitor-table"><thead><tr><th>Nama siswa</th><th>Username / email</th><th>Kelas</th><th>Latihan</th><th>Rata-rata</th><th>Aksi</th></tr></thead><tbody><tr v-for="student in adminStudents" :key="student.id" :class="{selected: selectedMonitorStudent?.id===student.id}"><td><b>{{ student.name }}</b></td><td><b>{{ student.username }}</b><small>{{ student.email }}</small></td><td>Kelas {{ student.grade }}</td><td>{{ student.completedQuizzes }}</td><td><span class="monitor-percent">{{ student.averagePercent }}%</span></td><td><div class="monitor-actions"><button type="button" class="monitor-view-btn" @click.stop="selectedMonitorStudent=student">Lihat nilai</button><button type="button" class="monitor-delete-btn" @click.stop="requestDeleteStudent(student)">Hapus</button></div></td></tr></tbody></table></div>
              <div v-else class="monitor-empty">Belum ada akun siswa yang terdaftar.</div>
            </section>
            <section v-if="selectedMonitorStudent" class="monitor-detail-card"><div class="monitor-card-heading"><div><b>{{ selectedMonitorStudent.name }}</b><small>{{ selectedMonitorStudent.email }} · Kelas {{ selectedMonitorStudent.grade }}</small></div><button class="monitor-close" aria-label="Tutup detail" @click="selectedMonitorStudent=null">×</button></div><div class="monitor-stat-row"><div><b>{{ selectedMonitorStudent.correctAnswers }}</b><small>Jawaban benar</small></div><div><b>{{ selectedMonitorStudent.wrongAnswers }}</b><small>Jawaban salah</small></div><div><b>{{ selectedMonitorStudent.averagePercent }}%</b><small>Rata-rata</small></div></div><h3>Riwayat latihan</h3><div v-if="selectedMonitorStudent.history.length" class="monitor-history"><div v-for="item in selectedMonitorStudent.history" :key="item.id" class="monitor-history-item"><div><b>{{ item.lessonTitle }}</b><small>{{ item.subjectName }} · {{ formatDate(item.completedAt) }}</small></div><strong>{{ item.score }}/{{ item.total }} · {{ item.percent }}%</strong></div></div><p v-else class="monitor-empty">Siswa ini belum menyelesaikan latihan.</p></section>
          </div>
          <div v-if="studentToDelete" class="delete-dialog-backdrop" @click.self="studentToDelete=null"><section class="delete-dialog" role="dialog" aria-modal="true" aria-labelledby="delete-dialog-title"><span class="delete-dialog-icon">!</span><h2 id="delete-dialog-title">Hapus akun siswa?</h2><p>Akun <b>{{ studentToDelete.name }}</b> (@{{ studentToDelete.username }}) dan seluruh riwayat nilainya akan dihapus.</p><div class="delete-dialog-actions"><button type="button" class="delete-cancel-btn" :disabled="!!deletingStudentId" @click="studentToDelete=null">Batal</button><button type="button" class="delete-confirm-btn" :disabled="!!deletingStudentId" @click="deleteStudent">{{ deletingStudentId ? 'Menghapus...' : 'Ya, hapus akun' }}</button></div></section></div>
        </template>
        <template v-else-if="selectedSubject">
          <button class="back-link" @click="home">← Semua mata pelajaran</button>
          <section class="section-heading"><div><div class="eyebrow">PILIH BAB ATAU MATERI</div><h1 class="subject-heading-title"><img :src="subjectLogo(selectedSubject.name)" :alt="selectedSubject.name" class="subject-heading-logo">{{ selectedSubject.name }}</h1><p>{{ selectedSubject.description }} · Kelas {{ selectedGrade }}</p></div><span class="progress-pill">{{ lessons.length }} bab tersedia</span></section>
          <div v-if="lessons.length" class="lesson-list"><div v-for="(lesson,i) in lessons" :key="lesson.id" class="lesson-card lesson-card-row"><button class="lesson-card-main" @click="openLesson(lesson)"><span class="lesson-number">{{ String(i+1).padStart(2,'0') }}</span><span class="lesson-info"><b>{{ lesson.title }}</b><small>Materi {{ i+1 }} · {{ lesson.grade }} · {{ lesson.estimatedMinutes }} menit</small></span><span class="lesson-open">→</span></button><button class="lesson-print-btn" @click="selectPractice(lesson,5)" aria-label="Mulai latihan singkat lima soal">5 soal</button><button class="lesson-print-btn" @click="previewPrint(lesson)" aria-label="Pratinjau dan cetak soal">Cetak soal</button></div></div>
          <div v-else class="empty-state">Belum ada materi {{ selectedSubject.name }} untuk kelas {{ selectedGrade }}.</div>
        </template>
        <template v-else>
          <section class="hero">
            <div class="hero-copy"><div class="eyebrow"><span class="sun-dot"></span> HARI INI ADALAH KESEMPATAN BARU</div><h1>{{ greeting }},<br><span>{{ studentName }}!</span> 👋</h1><p>Yuk, belajar hal baru hari ini. Sedikit demi sedikit, kamu pasti bisa!</p><button class="hero-btn" @click="scrollToId('subjects')">Mulai belajar <span>→</span></button><button v-if="account?.role!=='ADMIN'" class="donate-hero-btn" @click="openDonation">♡ Donasi</button><div class="hero-note">✦ Belajar seru · Tumbuh setiap hari</div></div>
            <div class="hero-art"><div class="art-sun"></div><div class="art-book">📚</div><div class="art-pencil">✏️</div><div class="art-star star-a">✦</div><div class="art-star star-b">✧</div><div class="art-cloud">☁️</div><div class="art-caption">Kamu pasti bisa!</div></div>
          </section>
          <section v-if="account?.role==='ADMIN'" class="grade-settings-card"><div><div class="eyebrow">TAMPILAN ADMIN</div><h2>Lihat materi per kelas</h2><p>Pilihan ini hanya mengubah tampilan admin. Latihan yang dikerjakan admin tidak disimpan sebagai progres.</p></div><form class="grade-settings-form" @submit.prevent><label for="admin-grade">Kelas</label><select id="admin-grade" :value="String(selectedGrade)" @change="changeAdminGrade($event.target.value)"><option v-for="grade in 6" :key="grade" :value="String(grade)">Kelas {{ grade }}</option></select></form></section>
          <section v-if="account?.role!=='ADMIN'" class="grade-settings-card"><div><div class="eyebrow">PENGATURAN BELAJAR</div><h2>Sudah naik kelas?</h2><p>Perbarui kelas untuk melihat materi dan mata pelajaran yang sesuai.</p><small v-if="gradeMessage" class="grade-feedback">{{ gradeMessage }}</small><small v-if="gradeError" class="grade-error">{{ gradeError }}</small></div><form class="grade-settings-form" @submit.prevent="saveGrade"><label for="student-grade">Kelas belajar</label><select id="student-grade" v-model="gradeForm"><option v-for="grade in 6" :key="grade" :value="String(grade)">Kelas {{ grade }}</option></select><button class="primary-btn" type="submit" :disabled="gradeSaving || String(gradeForm)===String(account.grade)">{{ gradeSaving ? 'Menyimpan...' : 'Simpan kelas' }}</button></form></section>
          <section class="quick-stats"><div class="stat-card"><span class="stat-icon stat-blue">📖</span><div><small>Mata pelajaran</small><b>{{ subjects.length }} bidang</b></div></div><div class="stat-card"><span class="stat-icon stat-yellow">🏅</span><div><small>Latihan selesai</small><b>{{ history.length }} latihan</b></div></div><div class="stat-card"><span class="stat-icon stat-green">🌱</span><div><small>Rata-rata nilai</small><b>{{ history.length ? Math.round(history.reduce((a,h)=>a+h.percent,0)/history.length) : 0 }}%</b></div></div></section>
          <section id="subjects" class="subject-section"><div class="section-title"><div><div class="eyebrow">PILIH DAN MULAI</div><h2>Mau belajar apa hari ini?</h2><p>Materi untuk kelas {{ selectedGrade }} ditampilkan {{ account?.role==='ADMIN' ? 'sesuai pilihan kelas admin' : 'sesuai akunmu' }}.</p></div><span class="section-decoration">✳</span></div>
            <div class="subject-grid"><button v-for="subject in subjects" :key="subject.id" class="subject-card" @click="openSubject(subject)"><span class="subject-icon" :style="{background:subject.color}"><img :src="subjectLogo(subject.name)" :alt="''" class="subject-logo-img"></span><span class="subject-name">{{ subject.name }}</span><span class="subject-description">{{ subject.description }}</span><span class="subject-bottom"><span>{{ subject.lessonCount }} bab</span><span class="circle-arrow">↗</span></span></button></div>
          </section>
          <section class="club-teaser"><span>✨</span><div><b>Belajar juga bisa lewat kegiatan yang kamu suka</b><small>{{ extracurricularCompleted }}/{{ extracurricularTotal }} kegiatan ekskul selesai · pilih klub sains, coding, cerita, atau seni</small></div><button @click="openExtracurricular">Jelajahi ekskul →</button></section>
          <section class="weekly-summary"><div><div class="eyebrow">UNTUK KELUARGA DAN GURU</div><h2>Ringkasan belajar minggu ini</h2><p>{{ studentName }} menyelesaikan <b>{{ weeklyHistory.length }} latihan</b> dalam 7 hari terakhir, dengan rata-rata <b>{{ weeklyHistory.length ? Math.round(weeklyHistory.reduce((sum,item)=>sum+item.percent,0)/weeklyHistory.length) : 0 }}%</b>. {{ weeklyHistory.length ? 'Terima kasih sudah berusaha dan terus belajar.' : 'Ajak anak memilih satu materi untuk mulai berlatih bersama.' }}</p><small v-if="summaryMessage">{{ summaryMessage }}</small></div><button class="secondary-btn" @click="copyWeeklySummary">Salin ringkasan</button></section>
          <section id="progress" class="progress-section"><div class="section-title"><div><div class="eyebrow">LANGKAH KECILMU</div><h2>Perkembanganku 🌱</h2><p>Setiap latihan adalah kemajuan.</p></div><span class="progress-pill">{{ history.length }} latihan selesai</span></div>
            <div v-if="history.length" class="history-list"><div v-for="item in history.slice(0,5)" :key="item.id" class="history-item"><span class="history-icon">✓</span><span class="history-info"><b>{{ item.lessonTitle }}</b><small>{{ item.subjectName }}</small></span><span class="history-score">{{ item.score }}/{{ item.total }} · {{ item.percent }}%</span></div></div>
            <div v-else class="empty-progress"><img class="progress-brand-logo" src="/teman-belajar-logo.png" alt="Logo Teman Belajar"><div><b>Perjalanan belajarmu dimulai dari sini!</b><p>Selesaikan satu latihan untuk melihat kemajuanmu.</p></div><button @click="scrollToId('subjects')">Mulai latihan →</button></div>
          </section>
          <footer class="footer">Dibuat dengan 💛 untuk anak Indonesia <span>Kenangan yang Indah tentang belajar!</span></footer>
        </template>
      </div>
      <div v-if="printPreview" class="print-preview-backdrop" @click.self="closePrintPreview"><section class="print-preview-dialog" role="dialog" aria-modal="true" aria-labelledby="print-title"><header class="print-preview-toolbar"><div><b>Pratinjau lembar soal</b><small>{{ printPreview.lesson.grade }} · {{ printPreview.lesson.subjectName }}</small></div><button class="print-close" @click="closePrintPreview">Tutup</button><button class="primary-btn print-action" :disabled="printLoading || !printPreview.questions.length" @click="printQuestions">Cetak / Simpan PDF</button></header><div v-if="printLoading" class="loading"><div class="spinner"></div><h3>Menyiapkan pratinjau...</h3></div><p v-else-if="printError" class="print-error">{{ printError }}</p><article v-else id="print-sheet" class="print-sheet"><h1 id="print-title">{{ printPreview.lesson.title }}</h1><p class="print-subtitle">{{ printPreview.lesson.subjectName }} · {{ printPreview.lesson.grade }}</p><div class="student-fields"><span>Nama: ______________________________</span><span>Tanggal: ______________</span><span>Nilai: ______ / {{ printPreview.questions.length }}</span></div><ol v-if="printPreview.questions.length" class="print-question-list"><li v-for="(question,index) in printPreview.questions" :key="index"><p>{{ question.prompt }}</p><ul class="print-options"><li v-for="(option,optIndex) in question.options" :key="optIndex"><span>{{ ['A','B','C','D'][optIndex] || optIndex+1 }}.</span> {{ option }}</li></ul><div class="answer-space" :class="{'essay-answer-space':question.type==='ESSAY'}"></div></li></ol><p v-else class="empty-state">Bab ini belum memiliki soal yang dapat dicetak.</p></article></section></div>
      <div v-if="donationOpen" class="donation-modal-backdrop" @click.self="donationOpen=false"><section class="donation-modal" role="dialog" aria-modal="true" aria-labelledby="donation-title"><button class="donation-modal-close" aria-label="Tutup" @click="donationOpen=false">×</button><div class="donation-heart">♡</div><div class="eyebrow">DUKUNG RUANG BELAJAR</div><h2 id="donation-title">Dukung Teman Belajar</h2><p>Terima kasih sudah membantu menjaga ruang belajar ini tetap bertumbuh.</p><img src="/QRIS_AR.jpeg" alt="QRIS donasi Teman Belajar" class="donation-qr-image"><small>Buka aplikasi pembayaran yang mendukung QRIS, lalu pindai kode ini.</small><button class="secondary-btn" @click="donationOpen=false">Tutup</button></section></div>
    </main>
  </div>
</template>
