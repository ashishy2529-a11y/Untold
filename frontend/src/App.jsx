import { useEffect, useMemo, useState } from 'react'
import { apiRequest, ensureCsrf } from './api'
import {
  ArrowUpRight, BookOpen, Feather, Heart, Home, LockKeyhole, Menu,
  MessageCircle, Plus, Search, Send, ShieldCheck, Sparkles, X
} from 'lucide-react'

const modules = [
  { id: 'void', name: 'THE VOID', group: 'EXPRESS', icon: Feather, desc: 'A quiet place to put your thoughts down.', detail: 'Write for yourself. Nothing is published unless you deliberately choose to share it.', action: 'Write privately', tone: 'violet' },
  { id: 'unsent', name: 'THE UNSENT', group: 'EXPRESS', icon: Send, desc: 'Letters you may never send.', detail: 'Say what stayed unsaid—to someone, to your past self, or simply to the page.', action: 'Write a letter', tone: 'blue' },
  { id: 'stories', name: 'SOMEONE, SOMEWHERE', group: 'COMMUNITY', icon: Heart, desc: 'A story can make someone feel less alone.', detail: 'A moderated space for people to share experiences without public profiles.', action: 'Explore stories', tone: 'pink' },
  { id: 'other-side', name: 'THE OTHER SIDE', group: 'COMMUNITY', icon: MessageCircle, desc: 'Be heard before being advised.', detail: 'Choose how you want people to respond: listen, understand, or offer advice.', action: 'Open conversations', tone: 'gold' },
  { id: 'tea-room', name: 'THE TEA ROOM', group: 'COMMUNITY', icon: Sparkles, desc: 'The lighter, messier side of being human.', detail: 'Funny and harmless confessions. No naming, shaming, harassment or doxxing.', action: 'Visit the tea room', tone: 'pink' },
  { id: 'mirror', name: 'THE MIRROR', group: 'REFLECT', icon: BookOpen, desc: 'Notice the patterns in your own words.', detail: 'An opt-in reflection space—not a diagnosis or a personality test.', action: 'Reflect', tone: 'blue' },
  { id: 'silent-match', name: 'THE SILENT MATCH', group: 'CONNECT', icon: Heart, desc: 'Find someone who understands the feeling.', detail: 'Topic-based discovery with mutual consent before any connection.', action: 'Discover connections', tone: 'violet' },
  { id: 'experiment', name: 'THE HUMAN EXPERIMENT', group: 'DISCOVER', icon: Search, desc: 'Small questions. Different perspectives.', detail: 'Voluntary, transparent polls and social experiments with aggregate results.', action: 'Explore experiments', tone: 'gold' },
  { id: 'capsule', name: 'THE TIME CAPSULE', group: 'REFLECT', icon: LockKeyhole, desc: 'Leave a thought for your future self.', detail: 'Save a thought and choose when you want it to return.', action: 'Create a capsule', tone: 'blue' }
]

const sampleStories = [
  { id: 1, category: 'GROWING UP', text: 'I used to think everyone else had a plan. Turns out most of us are just learning as we go.', time: 'A little while ago', hearts: 28 },
  { id: 2, category: 'SMALL WINS', text: 'Today I finally did the thing I had been putting off for months. It was smaller than I imagined, but it mattered.', time: 'Earlier today', hearts: 46 },
  { id: 3, category: 'LIFE', text: 'Sometimes a quiet evening and no expectations are exactly what you need.', time: 'Yesterday', hearts: 19 }
]

export default function App() {
  const [active, setActive] = useState('home')
  const [modal, setModal] = useState('')
  const [draft, setDraft] = useState('')
  const [entries, setEntries] = useState([])
  const [user, setUser] = useState(null)
  const [authReady, setAuthReady] = useState(false)
  const [authMode, setAuthMode] = useState('login')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [authError, setAuthError] = useState('')
  const [authBusy, setAuthBusy] = useState(false)
  const [stories, setStories] = useState(sampleStories)
  const [toast, setToast] = useState('')
  const [mobileNav, setMobileNav] = useState(false)
  const [responseMode, setResponseMode] = useState('JUST LISTEN')

  useEffect(() => {
    let active = true
    apiRequest('/api/auth/me').then(async account => {
      if (!active) return
      setUser(account)
      const [voidItems, letters] = await Promise.all([
        apiRequest('/api/private-writing/void'),
        apiRequest('/api/private-writing/unsent')
      ])
      if (active) setEntries([...voidItems, ...letters].map(item => ({ ...item, text: item.content, kind: item.type === 'UNSENT' ? 'UNSENT LETTER' : 'PRIVATE NOTE' })))
    }).catch(() => {}).finally(() => { if (active) setAuthReady(true) })
    return () => { active = false }
  }, [])

  async function handleAuthSubmit(event) {
    event.preventDefault()
    setAuthError('')
    setAuthBusy(true)
    try {
      await ensureCsrf()
      if (authMode === 'register') {
        await apiRequest('/api/auth/register', { method: 'POST', body: JSON.stringify({ email, password }) })
      }
      await apiRequest('/api/auth/login', { method: 'POST', body: JSON.stringify({ email, password }) })
      const account = await apiRequest('/api/auth/me')
      setUser(account)
      const [voidItems, letters] = await Promise.all([
        apiRequest('/api/private-writing/void'),
        apiRequest('/api/private-writing/unsent')
      ])
      setEntries([...voidItems, ...letters].map(item => ({ ...item, text: item.content, kind: item.type === 'UNSENT' ? 'UNSENT LETTER' : 'PRIVATE NOTE' })))
      setPassword('')
    } catch (error) {
      setAuthError(error.message)
    } finally { setAuthBusy(false) }
  }

  async function deleteEntry(item) {
    try {
      const type = item.type === 'UNSENT' ? 'unsent' : 'void'
      await apiRequest(`/api/private-writing/${type}/${item.id}`, { method: 'DELETE' })
      setEntries(current => current.filter(entry => entry.id !== item.id))
      notify('Your writing was deleted from your account.')
    } catch (error) { notify(error.message) }
  }

  async function handleLogout() {
    try { await apiRequest('/api/auth/logout', { method: 'POST' }) } catch {}
    setUser(null)
    setEntries([])
    setAuthMode('login')
    notify('You have been signed out.')
  }

  const currentModule = modules.find(m => m.id === modal)
  const savedCount = entries.length
  const nav = [
    { label: 'Home', id: 'home', icon: Home },
    { label: 'Express', id: 'express', icon: Feather },
    { label: 'Community', id: 'community', icon: Heart },
    { label: 'Reflect', id: 'reflect', icon: BookOpen },
    { label: 'Discover', id: 'discover', icon: Sparkles }
  ]
  const visibleModules = useMemo(() => active === 'home' ? modules : modules.filter(m =>
    (active === 'express' && m.group === 'EXPRESS') ||
    (active === 'community' && m.group === 'COMMUNITY') ||
    (active === 'reflect' && m.group === 'REFLECT') ||
    (active === 'discover' && ['CONNECT', 'DISCOVER'].includes(m.group))
  ), [active])

  function notify(message) {
    setToast(message)
    window.setTimeout(() => setToast(''), 2600)
  }

  async function saveEntry() {
    const text = draft.trim()
    if (!text || !user) return
    try {
      const type = modal === 'unsent' ? 'unsent' : 'void'
      const item = await apiRequest(`/api/private-writing/${type}`, {
        method: 'POST',
        body: JSON.stringify({ title: modal === 'unsent' ? 'An unsent letter' : 'Private note', content: text })
      })
      setEntries(current => [{ ...item, text: item.content, kind: type === 'unsent' ? 'UNSENT LETTER' : 'PRIVATE NOTE' }, ...current])
      setDraft('')
      setModal('')
      notify('Encrypted and saved to your account.')
    } catch (error) { notify(error.message) }
  }

  function publishStory() {
    const text = draft.trim()
    if (!text) return
    setStories([{ id: Date.now(), category: 'COMMUNITY', text, time: 'Just now', hearts: 0 }, ...stories])
    setDraft('')
    setModal('')
    notify('Prototype story added to this screen only—not published online.')
  }

  function openModule(id) {
    if (id === 'void' || id === 'unsent' || id === 'stories' || id === 'other-side') setModal(id)
    else notify('This module is mapped in the product; its full workflow is planned for a later build.')
  }

  return (
    <div className="app-shell">
      <aside className={`sidebar ${mobileNav ? 'sidebar-open' : ''}`}>
        <div className="brand"><div className="brand-mark">u.</div><div><div className="brand-name">UNTOLD<span>.</span></div><div className="brand-tag">EVERY STORY HAS A PLACE.</div></div><button className="icon-btn mobile-close" onClick={() => setMobileNav(false)} aria-label="Close menu"><X size={19}/></button></div>
        <div className="side-label">YOUR SPACE</div>
        <nav>{nav.map(item => <button key={item.id} className={`nav-item ${active === item.id ? 'selected' : ''}`} onClick={() => { setActive(item.id); setMobileNav(false) }}><item.icon size={17}/>{item.label}{item.id === 'home' && <span className="nav-dot"/>}</button>)}</nav>
        <div className="side-divider"/>
        <div className="side-label">YOUR CORNER</div>
        <button className="nav-item" onClick={() => { setModal('void'); setMobileNav(false) }}><LockKeyhole size={17}/>My private notes</button>
        <button className="nav-item" onClick={() => { setActive('my-space'); setMobileNav(false) }}><BookOpen size={17}/>My space <span className="count">{savedCount}</span></button>
        <div className="sidebar-bottom"><div className="privacy-card"><ShieldCheck size={17}/><div><strong>{user ? 'Encrypted private space.' : 'Account storage required.'}</strong><p>{user ? 'Your private entries are account-owned and encrypted in storage.' : 'Sign in to save private writing to your account.'}</p></div></div><div className="profile"><div className="avatar">U</div><div><strong>{user?.email || 'Guest explorer'}</strong><span>{user ? 'Signed in' : 'No account connected'}</span></div><button className="text-link" onClick={user ? handleLogout : () => setAuthMode('login')}>{user ? 'Sign out' : 'Sign in'}</button></div></div>
      </aside>

      <main className="main-area">
        <header className="topbar"><button className="icon-btn menu-toggle" onClick={() => setMobileNav(true)} aria-label="Open menu"><Menu size={20}/></button><div className="breadcrumb">YOUR SPACE <span>/</span> {nav.find(n => n.id === active)?.label.toUpperCase()}</div><div className="top-actions"><span className="private-pill"><LockKeyhole size={13}/> {user ? 'ACCOUNT SESSION' : 'PREVIEW MODE'}</span><button className="icon-btn" aria-label="Search" onClick={() => notify('Search will arrive with the community feed.') }><Search size={18}/></button></div></header>

        <div className="content">
          <div className="demo-notice"><ShieldCheck size={16}/><span><strong>{user ? 'Private account storage.' : 'Community preview mode.'}</strong> {user ? 'Your VOID and UNSENT entries are encrypted before database storage. Use a trusted device and sign out when finished.' : 'Sign in to use encrypted private writing. Community stories below are examples only.'}</span></div><section className="welcome"><div className="eyebrow"><span/> A LITTLE SPACE TO BE HUMAN</div><h1>There is more to<br/>you than <em>meets the eye.</em></h1><p>You don't have to have the right words. You don't have to be anyone in particular. Just begin wherever you are.</p><button className="primary-btn" onClick={() => setModal('void')}>Put it into words <ArrowUpRight size={16}/></button><div className="welcome-orbit orbit-one"/><div className="welcome-orbit orbit-two"/><div className="welcome-star">✳</div></section>

          <section className="section-head"><div><div className="eyebrow">FIND YOUR WAY IN</div><h2>{active === 'home' ? 'A place for every side of you.' : nav.find(n => n.id === active)?.label + ', at your pace.'}</h2><p>Every space stands on its own. Start anywhere.</p></div><span className="module-count">0{visibleModules.length} SPACES</span></section>
          {active !== 'my-space' && <section className="module-grid">{visibleModules.map((m, i) => <article className={`module-card ${m.tone}`} key={m.id} style={{'--delay': `${i * 45}ms`}}><div className="module-top"><div className="module-icon"><m.icon size={19}/></div><span className="module-group">{m.group}</span></div><h3>{m.name}</h3><p>{m.desc}</p><button className="text-link" onClick={() => openModule(m.id)}>{m.action}<ArrowUpRight size={15}/></button></article>)}</section>

          {active === 'home' && <section className="community-preview"><div className="section-head compact"><div><div className="eyebrow">A SHARED HUMAN THING</div><h2>Someone, somewhere.</h2><p>Small reminders that none of us are entirely alone.</p></div><button className="subtle-btn" onClick={() => setActive('community')}>See community <ArrowUpRight size={15}/></button></div><div className="story-list">{stories.slice(0,2).map(s => <article className="story-card" key={s.id}><div className="story-meta"><span>{s.category}</span><span>{s.time}</span></div><p>“{s.text}”</p><div className="story-foot"><span><Heart size={14}/> {s.hearts} people felt this</span><span>Shared anonymously</span></div></article>)}</div></section>}

          {active === 'community' && <section className="community-preview"><div className="section-head compact"><div><div className="eyebrow">COMMUNITY NOTES</div><h2>Stories from somewhere.</h2><p>Prototype examples only. Not connected to other users.</p></div><button className="primary-btn small" onClick={() => setModal('stories')}><Plus size={15}/> Share a story</button></div><div className="story-list">{stories.map(s => <article className="story-card" key={s.id}><div className="story-meta"><span>{s.category}</span><span>{s.time}</span></div><p>“{s.text}”</p><div className="story-foot"><span><Heart size={14}/> {s.hearts} people felt this</span><span>Anonymous preview</span></div></article>)}</div></section>}

          <footer><span>UNTOLD<span className="lavender">.</span> — EVERY STORY HAS A PLACE.</span><span>BUILT ON THE BELIEF THAT EVERYONE HAS A STORY.</span></footer>
        </div>
      </main>

      {!authReady && <div className="auth-loading">Opening your space…</div>}
      {authReady && !user && <div className="auth-gate"><form className="auth-card" onSubmit={handleAuthSubmit}><div className="brand-mark auth-mark">u.</div><div className="eyebrow">EVERY STORY HAS A PLACE</div><h2>{authMode === 'login' ? 'Welcome back.' : 'Make room for your story.'}</h2><p>Sign in to save private notes and unsent letters securely to your account.</p><label>Email address<input type="email" autoComplete="email" required maxLength={254} value={email} onChange={e => setEmail(e.target.value)} placeholder="you@example.com"/></label><label>Password<input type="password" autoComplete={authMode === 'login' ? 'current-password' : 'new-password'} required minLength={authMode === 'register' ? 12 : 1} maxLength={72} value={password} onChange={e => setPassword(e.target.value)} placeholder={authMode === 'register' ? 'At least 12 characters' : 'Your password'}/></label>{authError && <div className="auth-error" role="alert">{authError}</div>}<button className="primary-btn auth-submit" disabled={authBusy}>{authBusy ? 'Please wait…' : authMode === 'login' ? 'Sign in' : 'Create account'} <ArrowUpRight size={15}/></button><button type="button" className="auth-switch" onClick={() => { setAuthMode(authMode === 'login' ? 'register' : 'login'); setAuthError('') }}>{authMode === 'login' ? 'New to UNTOLD? Create an account' : 'Already have an account? Sign in'}</button><small>Use a unique password. Email verification and password recovery are not yet available in this development build.</small></form></div>}
      {mobileNav && <button className="scrim" onClick={() => setMobileNav(false)} aria-label="Close navigation"/>}
      {modal && <div className="modal-backdrop" onMouseDown={e => e.target === e.currentTarget && setModal('')}><section className="write-modal"><button className="modal-close icon-btn" onClick={() => setModal('')} aria-label="Close"><X size={19}/></button><div className="eyebrow">{currentModule?.group || 'YOUR PRIVATE SPACE'}</div><h2>{modal === 'void' ? 'Let it out.' : modal === 'unsent' ? 'A letter that can stay here.' : modal === 'stories' ? 'Your story has a place.' : 'What do you need right now?'}</h2><p className="modal-intro">{modal === 'stories' ? 'This demo adds your story to this browser only. A real public feed needs moderation and server-side publishing.' : modal === 'other-side' ? 'Choose the kind of response you want. This is a local prototype interaction.' : 'Take your time. There is no perfect way to begin.'}</p>
        {modal === 'other-side' && <div className="response-modes">{['JUST LISTEN','UNDERSTAND','ADVICE'].map(v => <button key={v} className={responseMode === v ? 'mode-active' : ''} onClick={() => setResponseMode(v)}>{v}</button>)}</div>}
        <textarea autoFocus value={draft} onChange={e => setDraft(e.target.value)} placeholder={modal === 'stories' ? 'A thought, a moment, a little truth...' : modal === 'unsent' ? 'Dear...' : 'Start anywhere...'} rows={7}/><div className="modal-bottom"><span><LockKeyhole size={13}/> {modal === 'stories' ? 'LOCAL PREVIEW — NOT ONLINE' : 'ENCRYPTED ACCOUNT STORAGE'}</span><button className="primary-btn" disabled={!draft.trim()} onClick={modal === 'stories' ? publishStory : saveEntry}>{modal === 'stories' ? 'Add preview story' : 'Save privately'} <ArrowUpRight size={15}/></button></div></section></div>}
      {toast && <div className="toast"><ShieldCheck size={16}/>{toast}</div>}
    </div>
  )
}
